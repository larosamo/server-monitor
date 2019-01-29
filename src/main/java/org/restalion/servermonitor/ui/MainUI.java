package org.restalion.servermonitor.ui;

import java.util.ArrayList;
import java.util.List;

import org.restalion.servermonitor.dto.MonitorDto;
import org.restalion.servermonitor.dto.ServerDto;
import org.restalion.servermonitor.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
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
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import lombok.extern.slf4j.Slf4j;

@Theme(value = Lumo.class, variant = Lumo.DARK)
@Route("dashboard")
@Slf4j
public class MainUI extends HorizontalLayout {
	
	ServerDto serverInfo;
	Binder<ServerDto> binder;
	Grid<MonitorDto> historic;
	Grid<MonitorDto> statusGrid;
	TextField name;
	TextField url;
	Checkbox active;

	public MainUI(@Autowired MonitorService service) {
		
		add(createLeftPanel(service));
		add(createRightPanel(service));
		
	}
	
	private VerticalLayout createRightPanel(MonitorService service) {
		VerticalLayout rightPanel = new VerticalLayout();
		serverInfo = ServerDto.builder().build();
		Label serverDetail = new Label("Server Details");
		name = new TextField("Name");
		name.setEnabled(Boolean.FALSE);
		url = new TextField("URL");
		url.setEnabled(Boolean.FALSE);
		url.setWidth("400px");
		active = new Checkbox("Active");
		active.setEnabled(Boolean.FALSE);
		Label serverHistoric = new Label("Historic Data");
		
		binder = new Binder<>();
		binder.forField(name).bind(ServerDto::getName, ServerDto::setName);
		binder.forField(url).bind(ServerDto::getUrl, ServerDto::setUrl);
		binder.forField(active).bind(ServerDto::getActive, ServerDto::setActive);
		
		binder.readBean(serverInfo);
		
		historic = new Grid<>();
		historic.setSelectionMode(SelectionMode.NONE);
		
		historic.addColumn(MonitorDto::getServerName).setHeader("Server Name");
		historic.addColumn(new IconRenderer<MonitorDto>(
                item -> item.getStatus().equals("OK") ? createCircle("GREEN")
                        : createCircle("RED"),
                item -> "")).setHeader("Status");
		historic.addColumn(MonitorDto::getTime).setHeader("Timestamp").setSortable(Boolean.TRUE);
		
		rightPanel.add(serverDetail);
		rightPanel.add(name);
		rightPanel.add(url);
		rightPanel.add(active);
		rightPanel.add(serverHistoric);
		rightPanel.add(historic);
		
		return rightPanel;
	}
	
	private VerticalLayout createLeftPanel(MonitorService service) {
		VerticalLayout leftPanel = new VerticalLayout();
		statusGrid = new Grid<>();
		Grid<ServerDto> monitoredServers = new Grid<>();
		Button editButton = new Button("Edit");
		Button saveButton = new Button("Save");
		
		log.debug("Número de servidores registrados: " + service.getServers().size());
		statusGrid.setItems(service.monitor());
		statusGrid.setSelectionMode(SelectionMode.NONE);
		statusGrid.addColumn(MonitorDto::getServerName).setHeader("Server Name");
		statusGrid.addColumn(new IconRenderer<MonitorDto>(
                item -> item.getStatus().equals("OK") ? createCircle("GREEN")
                        : createCircle("RED"),
                item -> "")).setHeader("Status");
		
		monitoredServers.setItems(service.getServers());
		monitoredServers.addColumn(ServerDto::getName).setHeader("Server Name");
		monitoredServers.setSelectionMode(SelectionMode.SINGLE);

		monitoredServers.addColumn(new IconRenderer<ServerDto>(
                item -> item.getActive() ? createCheck()
                        : createClose(),
                item -> "")).setHeader("Active");
		monitoredServers.addSelectionListener(listener -> {
			if (listener.getFirstSelectedItem().isPresent()) {
				serverInfo = listener.getFirstSelectedItem().get();
				binder.readBean(serverInfo);
				historic.setItems(service.historic(serverInfo.getName()));
				editButton.setEnabled(Boolean.TRUE);
			} else {
				serverInfo = ServerDto.builder().build();
				binder.readBean(serverInfo);
				historic.setItems(new ArrayList<MonitorDto>());
				editButton.setEnabled(Boolean.FALSE);
				name.setEnabled(Boolean.FALSE);
				url.setEnabled(Boolean.FALSE);
				active.setEnabled(Boolean.FALSE);
				saveButton.setEnabled(Boolean.FALSE);
			}
		});
		
		Button refreshButton = new Button("Refresh Status");
		refreshButton.setIcon(VaadinIcon.REFRESH.create());
		refreshButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
			
			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				statusGrid.setItems(service.monitor());
			}
		});
		
		editButton.setIcon(VaadinIcon.EDIT.create());
		editButton.setEnabled(Boolean.FALSE);
		editButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
			
			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				name.setEnabled(Boolean.TRUE);
				url.setEnabled(Boolean.TRUE);
				active.setEnabled(Boolean.TRUE);
				saveButton.setEnabled(Boolean.TRUE);
				editButton.setEnabled(Boolean.FALSE);
			}
		});
		
		saveButton.setIcon(VaadinIcon.INSERT.create());
		saveButton.setEnabled(Boolean.FALSE);
		saveButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
			
			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				System.out.println("Saving");
				name.setEnabled(Boolean.FALSE);
				url.setEnabled(Boolean.FALSE);
				active.setEnabled(Boolean.FALSE);
				saveButton.setEnabled(Boolean.FALSE);
				editButton.setEnabled(Boolean.TRUE);
			}
		});
		
		HorizontalLayout buttonPanel = new HorizontalLayout();
		
		buttonPanel.add(refreshButton);
		buttonPanel.add(editButton);
		buttonPanel.add(saveButton);
		leftPanel.add(buttonPanel);
		leftPanel.add(new Label("Monitored Servers"));
		leftPanel.add(monitoredServers);
		leftPanel.add(new Label("Current Status"));
		leftPanel.add(statusGrid);
		
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
