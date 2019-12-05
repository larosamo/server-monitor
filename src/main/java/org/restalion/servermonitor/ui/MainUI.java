package org.restalion.servermonitor.ui;

import java.util.ArrayList;

import org.restalion.servermonitor.dto.MonitorDto;
import org.restalion.servermonitor.dto.ServerDto;
import org.restalion.servermonitor.service.MonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import lombok.extern.slf4j.Slf4j;

@Push
@Theme(value = Lumo.class, variant = Lumo.DARK)
@Route("dashboard")
@Slf4j
@Configuration
@UIScope
public class MainUI extends HorizontalLayout {
	
    private StatusThread thread;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        // Start the data feed thread
    	Boolean threadFound = false;
    	Thread[] threads = new Thread[Thread.activeCount()];
    	Thread.enumerate(threads);
    	for (Thread  it : threads) {
			if (it != null && it.getName().equals("statusThread")) {
				log.debug("Thread statusThread is already running");
				threadFound = true;
			}
		}
    	if (!threadFound) {
	    	log.debug("Creamos nuevo Thread");
        	thread = new StatusThread(attachEvent.getUI(), this, serviceMonitor);
	        thread.setName("statusThread");
        	thread.start();
	        log.debug("Thread " + thread.getName() + " creado");
        } 
    }
    
    @Override
    protected void onDetach(DetachEvent detachEvent) {
        // Cleanup
       if (thread != null) {
    	thread.interrupt();
        thread = null;
       }
    }
    
    private static class StatusThread extends Thread {
        private final UI ui;
        private final MainUI view;
        MonitorService threadService;
       
        public StatusThread(UI ui, MainUI view, MonitorService service) {
            this.ui = ui;
            this.view = view;
            threadService = service;
            
        }

        @Override
        public void run() {
        	try {
        		// Update the data for a while
                while (true) {
                	Thread.sleep(view.timeInterval);
                    log.debug("Refreshing status ");

                    ui.access(() -> view.statusGrid.setItems(threadService.lastStatus()));
                    if (!view.name.getValue().isEmpty()) {
                    	ui.access(() -> view.historic.setItems(threadService.historic(view.name.getValue())));
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
	
	@Autowired
	MonitorService serviceMonitor;
	ServerDto serverInfo;
	Binder<ServerDto> binder;
	Binder<MonitorDto> binderMonitor;
	Grid<MonitorDto> historic;
	Grid<MonitorDto> statusGrid;
	Grid<ServerDto> monitoredServers;
	TextField name;
	TextField url;
	Checkbox active;
	Button serverButton = new Button(" Servers Management");
	@Value("${fixedRate}")
    private long timeInterval;
	
	public MainUI(@Autowired MonitorService service) {
		
		add(createLeftPanel(service));
		add(createRightPanel(service));
		setSizeFull();
		
	}

	private VerticalLayout createRightPanel(MonitorService service) {
		setHeight("800px");
		VerticalLayout rightPanel = new VerticalLayout();
		serverInfo = ServerDto.builder().build();
		HorizontalLayout serverlayout = new HorizontalLayout();
		Label serverDetail = new Label("Server Details");
		serverlayout.add(serverDetail);
		serverButton.setIcon(VaadinIcon.COG.create());
		serverButton.addClickListener(e -> {
			UI.getCurrent().navigate("setup");
		});
		serverlayout.add(serverButton);
		serverlayout.setAlignItems(Alignment.BASELINE);
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
		
		rightPanel.add(serverlayout);
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
		monitoredServers = new Grid<>();
		Button editButton = new Button("Edit");
		Button saveButton = new Button("Save");
		Button addButton = new Button("Add");
		Button deleteButton = new Button("Delete");
		binderMonitor = new Binder<>();
		
		
		log.debug("Número de servidores registrados: " + service.getServers().size());
		statusGrid.setItems(service.monitor());
		statusGrid.setSelectionMode(SelectionMode.NONE);
		statusGrid.addColumn(MonitorDto::getServerName).setHeader("Server Name");
		statusGrid.addColumn(new IconRenderer<MonitorDto>(
                item -> item.getStatus().equals("OK") ? createCircle("GREEN")
                        : createCircle("RED"),
                item -> "")).setHeader("Status");
		statusGrid.setHeightByRows(true);
		
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
				deleteButton.setEnabled(Boolean.TRUE);
			} else {
				serverInfo = ServerDto.builder().build();
				binder.readBean(serverInfo);
				historic.setItems(new ArrayList<MonitorDto>());
				editButton.setEnabled(Boolean.FALSE);
				deleteButton.setEnabled(Boolean.FALSE);
				name.setEnabled(Boolean.FALSE);
				url.setEnabled(Boolean.FALSE);
				active.setEnabled(Boolean.FALSE);
				saveButton.setEnabled(Boolean.FALSE);
			}
		});
		monitoredServers.setHeightByRows(true);
		
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
				deleteButton.setEnabled(Boolean.FALSE);
			}
		});
		
		saveButton.setIcon(VaadinIcon.INSERT.create());
		saveButton.setEnabled(Boolean.FALSE);
		saveButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
			
			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				name.setEnabled(Boolean.FALSE);
				url.setEnabled(Boolean.FALSE);
				active.setEnabled(Boolean.FALSE);
				saveButton.setEnabled(Boolean.FALSE);
				editButton.setEnabled(Boolean.TRUE);
				
				if (monitoredServers.getSelectedItems().isEmpty()) {
					ServerDto dto = ServerDto.builder()
							.active(active.getValue())
							.name(name.getValue())
							.url(url.getValue())
							.build();
					service.save(dto);
				} else {
					monitoredServers.getSelectedItems().forEach(server -> {
						
						ServerDto dto = ServerDto.builder()
								.active(active.getValue())
								.name(name.getValue())
								.url(url.getValue())
								.id(server.getId())
								.build();
						
						service.save(dto);
					});
				}
				
				monitoredServers.setItems(service.getServers());
				statusGrid.setItems(service.monitor());
			}
		});
		
		addButton.setIcon(VaadinIcon.PLUS_SQUARE_O.create());
		addButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				monitoredServers.select(null);
				editButton.setEnabled(Boolean.FALSE);
				saveButton.setEnabled(Boolean.TRUE);
				name.setEnabled(Boolean.TRUE);
				url.setEnabled(Boolean.TRUE);
				active.setEnabled(Boolean.TRUE);
			}
		});
		
		deleteButton.setIcon(VaadinIcon.CLOSE.create());
		deleteButton.setEnabled(Boolean.FALSE);
		deleteButton.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {

			@Override
			public void onComponentEvent(ClickEvent<Button> event) {
				
				monitoredServers.getSelectedItems().forEach(server -> {
					service.remove(server);
					monitoredServers.setItems(service.getServers());
				});
			}

		});
		
		
		HorizontalLayout buttonPanel = new HorizontalLayout();
		
		buttonPanel.add(refreshButton);
		buttonPanel.add(addButton);
		buttonPanel.add(editButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(deleteButton);
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
