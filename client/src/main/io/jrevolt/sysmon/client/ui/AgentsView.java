package io.jrevolt.sysmon.client.ui;

import io.jrevolt.sysmon.model.AgentInfo;
import io.jrevolt.sysmon.rest.RestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import static io.jrevolt.sysmon.client.ui.FxHelper.*;

/**
 * @author <a href="mailto:patrikbeno@gmail.com">Patrik Beno</a>
 */
@Component
public class AgentsView extends Base<BorderPane> {

	@FXML TableView<UIAgentInfo> table;

	@FXML TableColumn<UIAgentInfo, String> cluster;
	@FXML TableColumn<UIAgentInfo, String> server;
	@FXML TableColumn<UIAgentInfo, String> status;
	@FXML TableColumn<UIAgentInfo, String> version;
	@FXML TableColumn<UIAgentInfo, Instant> lastUpdated;
	@FXML TableColumn<UIAgentInfo, UIAgentInfo> actions;


	///

	@Autowired
	RestService rest;

	@Autowired
	WebTarget arest;

	///

	Map<String, UIAgentInfo> uiagents = new HashMap<>();

	@Override
	protected void initialize() {
		async(this::refresh);
	}

	@FXML
	void refresh() {
		List<AgentInfo> agents = rest.getAgentInfo();
//		ObservableList<UIAgentInfo> uiagents = FXCollections.observableArrayList();
		uiagents.clear();
		agents.forEach(a -> uiagents.put(a.getServer(), new UIAgentInfo(a)));
		fxasync(()->{
			table.setItems(FXCollections.observableArrayList(uiagents.values()));
			cluster.setCellValueFactory(new PropertyValueFactory<>("cluster"));
			server.setCellValueFactory(new PropertyValueFactory<>("server"));
			status.setCellValueFactory(new PropertyValueFactory<>("status"));
			version.setCellValueFactory(new PropertyValueFactory<>("version"));
			lastUpdated.setCellValueFactory(new PropertyValueFactory<>("lastUpdated"));
			actions.setCellFactory(param -> new TableCell<UIAgentInfo, UIAgentInfo>() {
				@Override
				protected void updateItem(UIAgentInfo item, boolean empty) {
					if (empty) {
						setText(null);
						setGraphic(null);
					} else {
						Button bRestart = new Button("restart");
						bRestart.setOnAction(e->restartSingle());

						Button bCheck  = new Button("ping");
						bCheck.setOnAction(e->pingAgent(table.getItems().get(getIndex())));

						HBox box = new HBox();
						box.getChildren().addAll(bRestart, bCheck);

						setGraphic(box);
						setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					}
				}
			});
			Preferences prefs = Preferences.userNodeForPackage(AgentsView.class);
			table.getColumns().forEach(c -> {
				c.prefWidthProperty().set(prefs.getDouble(c.getId(), 70));
				c.widthProperty().addListener((observable, oldValue, newValue) -> {
					prefs.putDouble(c.getId(), c.getWidth());
				});
			});
		});
	}

	@FXML
	void restartSelectedAgents() {
	}

	void restartSingle() {
		async(rest::restart);
	}

	void pingAgent(UIAgentInfo item) {
		async(() -> {
			try {
				fxasync(()-> item.status().set(AgentInfo.Status.CHECKING.name()));
				Future<AgentInfo> f = arest.path("ping").path(item.getServer()).request().async().get(AgentInfo.class);
				AgentInfo info = f.get(10, TimeUnit.SECONDS);
				fxupdate(() -> uiagents.get(item.getServer()).update(info));
			} catch (Exception e) {
				throw new UnsupportedOperationException(e);
			}
		});
	}
}