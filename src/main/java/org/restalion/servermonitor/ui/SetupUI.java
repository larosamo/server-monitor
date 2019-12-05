package org.restalion.servermonitor.ui;

import java.util.Set;

import org.restalion.servermonitor.dto.LastReportSentDto;
import org.restalion.servermonitor.dto.ManagerDto;
import org.restalion.servermonitor.dto.MonitorDto;
import org.restalion.servermonitor.dto.ServerDto;
import org.restalion.servermonitor.dto.ServerManagerDto;
import org.restalion.servermonitor.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import lombok.extern.slf4j.Slf4j;

@Theme(value = Lumo.class, variant = Lumo.DARK)
@Route("setup")
@Slf4j
@Configuration
@UIScope
public class SetupUI extends HorizontalLayout {
	
	
	ManagerDto managerInfo;
	ServerManagerDto serverManagerDto;
	Binder<ManagerDto> binder;
	Binder<ServerManagerDto> binderManagedServers;
	Binder<MonitorDto> binderMonitor;
	Grid<ServerManagerDto> managedServersGrid;
	Grid<ManagerDto> managersGrid;
	Grid<ServerDto> serversGrid;
	TextField name;
	TextField managerName;
	TextField serverSelected;
	TextField mail;
	Checkbox active;
	ComboBox<ServerDto> serversCombo = new ComboBox<>();
	Label labelaux = new Label(" ");
	@Value("${fixedRate}") 
    private long timeInterval;
	
	public SetupUI(@Autowired MonitorService service) {
		add(createLeftPanel(service));
		add(createRightPanel(service));
		setSizeFull();
		
	}

	private VerticalLayout createRightPanel(MonitorService service) {
		setHeight("800px");
		VerticalLayout rightPanel = new VerticalLayout();
		managerInfo = ManagerDto.builder().build();
		Label managerDetail = new Label("Manager Details");
		name = new TextField("Name");
		name.setEnabled(Boolean.FALSE);
		mail = new TextField("Mail");
		mail.setEnabled(Boolean.FALSE);
		mail.setWidth("400px");
		active = new Checkbox("Active");
		active.setEnabled(Boolean.FALSE);
		Button addRelationButton = new Button("");
		Button deleteRelationButton = new Button("");
		Button editRelationButton = new Button("");
		
		//label serverHistoric = new Label("Historic Data");
		
		binder = new Binder<>();
		binder.forField(name).bind(ManagerDto::getName, ManagerDto::setName);
		binder.forField(mail).bind(ManagerDto::getMail, ManagerDto::setMail);
		binder.forField(active).bind(ManagerDto::getActive, ManagerDto::setActive);
		
		binder.readBean(managerInfo);
		
		Label managedServersLabel = new Label(" Managed Servers by User");
		HorizontalLayout relationPanel = new HorizontalLayout();
		
		binderManagedServers = new Binder<>();
		managerName = new TextField("Manager Name");
		managerName.setReadOnly(true);
		binderManagedServers.forField(managerName).bind(ServerManagerDto::getManagerName, ServerManagerDto::setManagerName);
		serverSelected = new TextField();
		serverSelected.setVisible(false);
		serversCombo.setLabel("Server");
		serversCombo.setItemLabelGenerator(ServerDto::getName);
		serversCombo.setItems(service.getAvailableServerByManager(managerName.getValue()));
		serversCombo.addValueChangeListener(event -> {
			ServerDto server = serversCombo.getValue();
			if (server != null) {
		    	serverSelected.setValue(server.getName());
		    	addRelationButton.setEnabled(Boolean.TRUE);
		    } else {
		    	addRelationButton.setEnabled(Boolean.FALSE);
		    }
		});
		binderManagedServers.forField(serverSelected).bind(ServerManagerDto::getServerName, ServerManagerDto::setServerName);
		managedServersGrid = new Grid<>();
		managedServersGrid.setSelectionMode(SelectionMode.MULTI);
		managedServersGrid.addSelectionListener(listener -> {
			if (listener.getFirstSelectedItem().isPresent()) {
				deleteRelationButton.setEnabled(Boolean.TRUE);
				editRelationButton.setEnabled(Boolean.TRUE);
			} else {
				deleteRelationButton.setEnabled(Boolean.FALSE);
				editRelationButton.setEnabled(Boolean.FALSE);
			}
		});
		
		managedServersGrid.addColumn(ServerManagerDto::getServerName).setHeader("Server Name");
		managedServersGrid.addColumn(new IconRenderer<ServerManagerDto>(
				item -> item.getActive() ? createCheck()
                        : createClose(),
                item -> "")).setHeader("Notification Status");


		addRelationButton.setIcon(VaadinIcon.PLUS_SQUARE_O.create());
		addRelationButton.setEnabled(Boolean.FALSE);
		addRelationButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
		
			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				addRelationButton.setEnabled(Boolean.FALSE);
				if (((managerName.getValue().length() >0) && (serverSelected.getValue().length()>0))) {
					ServerManagerDto dto = ServerManagerDto.builder()
							.active(true)
							.managerName(managerName.getValue())
							.serverName(serverSelected.getValue())
							.build();
					service.save(dto);
					LastReportSentDto lastSendDto = LastReportSentDto.builder()
							.managerName(managerName.getValue())
							.serverName(serverSelected.getValue()).
							build();
					service.save(lastSendDto);
					managedServersGrid.setItems(service.getManagedServersByManager(managerName.getValue()));
					serversCombo.setItems(service.getAvailableServerByManager(managerName.getValue()));
				} 
				serverSelected.setValue("");
			}
		});

		deleteRelationButton.setIcon(VaadinIcon.TRASH.create());
		deleteRelationButton.setEnabled(Boolean.FALSE);
		deleteRelationButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				if (managedServersGrid.getSelectedItems().size()>0) {
					managedServersGrid.getSelectedItems().forEach( managedServer -> {
						service.remove(managedServer);
						LastReportSentDto lastSendDto = LastReportSentDto.builder()
								.managerName(managedServer.getManagerName())
								.serverName(managedServer.getServerName())
								.build();
						service.remove(lastSendDto);
						managedServersGrid.deselect(managedServer);
					});
				managedServersGrid.setItems(service.getManagedServersByManager(managerName.getValue()));
				managedServersGrid.setSelectionMode(SelectionMode.NONE);
				managedServersGrid.setSelectionMode(SelectionMode.MULTI);
				serversCombo.setItems(service.getAvailableServerByManager(managerName.getValue()));
				}
				deleteRelationButton.setEnabled(Boolean.FALSE);
				editRelationButton.setEnabled(Boolean.FALSE);
			}
		});
		
		editRelationButton.setIcon(VaadinIcon.CHECK_SQUARE_O.create());
		editRelationButton.setEnabled(Boolean.FALSE);
		editRelationButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				Set<ServerManagerDto> selected = managedServersGrid.getSelectedItems();
				if (selected.size()>0) {
					selected.forEach( managedServer -> {
						if (managedServer.getActive()) {
							managedServer.setActive(Boolean.FALSE);
						} else {
							managedServer.setActive(Boolean.TRUE);
						}
						service.save(managedServer);
						LastReportSentDto lastSendDto = LastReportSentDto.builder()
								.managerName(managedServer.getManagerName())
								.serverName(managedServer.getServerName()).
								build();
						service.save(lastSendDto);
					});
					managedServersGrid.setItems(service.getManagedServersByManager(managerName.getValue()));
					managedServersGrid.setSelectionMode(SelectionMode.NONE);
					managedServersGrid.setSelectionMode(SelectionMode.MULTI);
				}
				deleteRelationButton.setEnabled(Boolean.FALSE);
				editRelationButton.setEnabled(Boolean.FALSE);
			}
		});

		relationPanel.add(managerName);
		relationPanel.add(serversCombo);
		relationPanel.add(addRelationButton);
		relationPanel.add(deleteRelationButton);
		relationPanel.add(editRelationButton);
		relationPanel.setAlignItems(Alignment.BASELINE);
		
				
		rightPanel.add(managerDetail);
		rightPanel.add(name);
		rightPanel.add(mail);
		rightPanel.add(active);
		rightPanel.add(managedServersLabel);
		rightPanel.add(relationPanel);
		rightPanel.add(managedServersGrid);
		
		return rightPanel;
	}	
	private VerticalLayout createLeftPanel(MonitorService service) {
		VerticalLayout leftPanel = new VerticalLayout();
		managersGrid = new Grid<>();
		serversGrid = new Grid<>();
		Button deleteButton = new Button("Delete");
		Button editButton = new Button("Edit");
		Button saveButton = new Button("Save");
		Button addButton = new Button("Add");
		Button dashboardButton = new Button("Dashboard");
		dashboardButton.setIcon(VaadinIcon.DESKTOP.create());
		dashboardButton.addClickListener(e -> {
			UI.getCurrent().navigate("dashboard");
			managersGrid.deselectAll();
			
		});
		
		
		managersGrid.setItems(service.getManagers());
		managersGrid.setSelectionMode(SelectionMode.SINGLE);
		managersGrid.addColumn(ManagerDto::getName).setHeader("Name");
		managersGrid.addColumn(ManagerDto::getMail).setHeader("Mail");
		managersGrid.addColumn(new IconRenderer<ManagerDto>(
				item -> item.getActive() ? createCheck()
                        : createClose(),
                item -> "")).setHeader("Active");
		managersGrid.addSelectionListener(listener -> {
			if (listener.getFirstSelectedItem().isPresent()) {
				managerInfo = listener.getFirstSelectedItem().get();
				binder.readBean(managerInfo);
				managerName.setValue(managerInfo.getName());
				serversCombo.setItems(service.getAvailableServerByManager(managerName.getValue()));
				managedServersGrid.setItems(service.getManagedServersByManager(managerName.getValue()));
				//historic.setItems(service.historic(serverInfo.getName()));
				editButton.setEnabled(Boolean.TRUE);
				deleteButton.setEnabled(Boolean.TRUE);
			} else {
				managerInfo = ManagerDto.builder().build();
				binder.readBean(managerInfo);
				//historic.setItems(new ArrayList<MonitorDto>());
				editButton.setEnabled(Boolean.FALSE);
				deleteButton.setEnabled(Boolean.FALSE);
				name.setEnabled(Boolean.FALSE);
				mail.setEnabled(Boolean.FALSE);
				active.setEnabled(Boolean.FALSE);
				saveButton.setEnabled(Boolean.FALSE);
				managerName.setValue("");
				serversCombo.setItems();
				managedServersGrid.setItems();				
			}
		});
		managersGrid.setHeightByRows(true);
		
		serversGrid.setItems(service.getServers());
		serversGrid.addColumn(ServerDto::getName).setHeader("Server Name");
		serversGrid.setSelectionMode(SelectionMode.SINGLE);

		serversGrid.addColumn(new IconRenderer<ServerDto>(
				item -> item.getActive() ? createCheck()
                        : createClose(),
                item -> "")).setHeader("Active");
		
		serversGrid.setHeightByRows(true);
		
		editButton.setIcon(VaadinIcon.EDIT.create());
		editButton.setEnabled(Boolean.FALSE);
		editButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
			
			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				name.setEnabled(Boolean.TRUE);
				mail.setEnabled(Boolean.TRUE);
				active.setEnabled(Boolean.TRUE);
				saveButton.setEnabled(Boolean.TRUE);
				editButton.setEnabled(Boolean.FALSE);
				deleteButton.setEnabled(Boolean.FALSE);
			}
		});
		
		saveButton.setIcon(VaadinIcon.INSERT.create());
		saveButton.setEnabled(Boolean.FALSE);
		saveButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
			
			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				name.setEnabled(Boolean.FALSE);
				mail.setEnabled(Boolean.FALSE);
				active.setEnabled(Boolean.FALSE);
				saveButton.setEnabled(Boolean.FALSE);
				//editButton.setEnabled(Boolean.TRUE);
				
				//Creation mode
				if (managersGrid.getSelectedItems().isEmpty()) {
					ManagerDto dto = ManagerDto.builder()
							.active(active.getValue())
							.name(name.getValue())
							.mail(mail.getValue())
							.build();
					service.save(dto);
				} else {
					//Edit mode
					managersGrid.getSelectedItems().forEach(manager -> {
						ManagerDto dto = ManagerDto.builder()
								.active(active.getValue())
								.name(name.getValue())
								.mail(mail.getValue())
								.id(manager.getId())
								.build();
						
						service.save(dto);
						
					});
				}
				
				managersGrid.setItems(service.getManagers());
				
			}
		});
		
		
		addButton.setIcon(VaadinIcon.PLUS_SQUARE_O.create());
		addButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				managersGrid.select(null);
				//editButton.setEnabled(Boolean.FALSE);
				saveButton.setEnabled(Boolean.TRUE);
				name.setEnabled(Boolean.TRUE);
				mail.setEnabled(Boolean.TRUE);
				active.setEnabled(Boolean.TRUE);
			}
		});
		
		deleteButton.setIcon(VaadinIcon.TRASH.create());
		deleteButton.setEnabled(Boolean.FALSE);
		deleteButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {

			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				
				managersGrid.getSelectedItems().forEach(manager -> {
					service.remove(manager);
					managersGrid.setItems(service.getManagers());
					managersGrid.setItems(service.getManagers());
				});
			}

		});
		
		
		HorizontalLayout buttonPanel = new HorizontalLayout();
		
		buttonPanel.add(addButton);
		buttonPanel.add(editButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(dashboardButton);
		
		
		leftPanel.add(buttonPanel);
		leftPanel.add(new Label("Managers"));
		leftPanel.add(managersGrid);
		leftPanel.add(new Label("Available Servers"));
		leftPanel.add(serversGrid);
		
		return leftPanel;
	}
	
	private Icon createCheck() {
		Icon check = VaadinIcon.CHECK.create();
		check.setColor("GREEN");
		return check;
	}
	
	private Icon createClose() {
		Icon close = VaadinIcon.CLOSE.create();
		close.setColor("GREY");
		return close;
	}
	
	private Icon createCircle(String color) {
		Icon circle = VaadinIcon.CIRCLE.create();
		circle.setColor(color);
		return circle;
	}
}
