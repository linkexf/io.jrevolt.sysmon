package io.jrevolt.sysmon.client.ui;

import io.jrevolt.sysmon.model.DomainDef;
import io.jrevolt.sysmon.model.EndpointStatus;
import io.jrevolt.sysmon.model.EndpointType;
import io.jrevolt.sysmon.rest.ApiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import org.apache.commons.lang3.StringUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.regex.Pattern;

import static io.jrevolt.sysmon.client.ui.FxHelper.*;

/**
 * @author <a href="mailto:patrikbeno@gmail.com">Patrik Beno</a>
 */
@Component
@EnableScheduling
public class EndpointsView extends Base<BorderPane> {

	@FXML TableView<Endpoint> table;

	@FXML TableColumn<Endpoint, String> cluster;
	@FXML TableColumn<Endpoint, String> server;
	@FXML TableColumn<Endpoint, String> artifact;
	@FXML TableColumn<Endpoint, URI> uri;
	@FXML TableColumn<Endpoint, EndpointType> type;
	@FXML TableColumn<Endpoint, EndpointStatus> status;
	@FXML TableColumn<Endpoint, String> comment;

	@FXML TextField filter;

	@Autowired
	ApiService api;

	Map<URI, Endpoint> endpointsByUri = new LinkedHashMap<>();

	ScheduledFuture<?> updater;

	@Override
	protected void initialize() {

		if (updater != null) {
			updater.cancel(true);
			updater = null;
		}

		super.initialize();

		cluster.setCellValueFactory(new PropertyValueFactory<>("cluster"));
		server.setCellValueFactory(new PropertyValueFactory<>("server"));
		artifact.setCellValueFactory(new PropertyValueFactory<>("artifact"));
		uri.setCellValueFactory(new PropertyValueFactory<>("uri"));
		type.setCellValueFactory(new PropertyValueFactory<>("type"));
		status.setCellValueFactory(new PropertyValueFactory<>("status"));
		comment.setCellValueFactory(new PropertyValueFactory<>("comment"));

		registerLayoutPersistor(table);

		async(() ->{
			load();
//			updater = FxHelper.scheduler().scheduleAtFixedRate(this::update, 1, 1, TimeUnit.SECONDS);
		});
	}

	@Override
	protected void close() {
		updater.cancel(true);
		super.close();
	}

	void load() {
		fxasync(()->pane.setCenter(new Text("Loading...")));

		final ObservableList<Endpoint> data = FXCollections.observableArrayList();
		final FilteredList<Endpoint> filtered = new FilteredList<>(data);
		final SortedList<Endpoint> sorted = new SortedList<>(filtered);

		sorted.comparatorProperty().bind(table.comparatorProperty());
		filter.textProperty().addListener((observable, oldValue, newValue) -> {
			filtered.setPredicate(this::filter);
		});

		DomainDef domain = api.getDomainDef();
		domain.getClusters().stream()
				.flatMap(c -> c.getServers().stream())
				.flatMap(s -> s.getProvides().stream())
				.forEach(e -> {
					Endpoint endpoint = new Endpoint();
					endpoint.setCluster(e.getCluster());
					endpoint.setServer(e.getServer());
					endpoint.setUri(e.getUri());
					endpoint.setStatus(e.getStatus());
					endpoint.setComment(e.getComment());
					data.add(endpoint);
					endpointsByUri.put(endpoint.getUri(), endpoint);
				});

		fxasync(() -> {
			table.setItems(sorted);
			pane.setCenter(table);
		});
	}

	void update() {
		if (!isVisible(pane)) { return; }

		Instant now = Instant.now();
		fxasync(() -> FxHelper.status().set("Updating..."));

		DomainDef domain = api.getDomainDef();
		fxasync(() -> {
			domain.getClusters().forEach(c -> c.getServers().forEach(s -> c.getProvides().forEach(e -> {
				Endpoint endpoint = endpointsByUri.get(e.getUri());
				endpoint.setStatus(e.getStatus());
				endpoint.setComment(e.getComment());
			})));
			//http://stackoverflow.com/questions/11065140/javafx-2-1-tableview-refresh-items
			status.setVisible(false);
			status.setVisible(true);
			FxHelper.status().set("Ready (updating every second). Last update in "+Duration.between(now, Instant.now()));
		});
	}

	@FXML
	void onFilterUpdate() {
		((FilteredList<Endpoint>) table.getItems()).setPredicate(this::filter);
	}

	boolean filter(Endpoint e) {
		Pattern filter = Pattern.compile(".*" + StringUtils.trimToEmpty(this.filter.getText()) + ".*", Pattern.DOTALL);
		String s = new StringBuilder()
				.append(Objects.toString(e.getUri(), "")).append("\t")
				.append(Objects.toString(e.getServer(), "")).append("\t")
				.append(Objects.toString(e.getStatus(), "")).append("\t")
				.append(Objects.toString(e.getComment(), "")).append("\t")
				.toString();
		return filter.matcher(s).matches();
	}
}
