package io.jrevolt.sysmon.client.ui;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import io.jrevolt.sysmon.client.ClientConfig;
import io.jrevolt.sysmon.core.AppCfg;
import io.jrevolt.sysmon.core.SpringBootApp;
import org.springframework.beans.factory.annotation.Autowired;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author <a href="mailto:patrikbeno@gmail.com">Patrik Beno</a>
 * @version $Id$
 */
public class FxMain extends Application {

	static private FxMain INSTANCE;

	static public FxMain instance() {
		return INSTANCE;
	}

	static public Stage stage() {
		return INSTANCE.stage;
	}

	static public void main(String[] args) {
		Application.launch(FxMain.class, args);
	}

	Stage stage;

	@Autowired
	AppCfg app;

	@Autowired
	ClientConfig client;

	{
		SpringBootApp.instance().autowire(this);
	}

	@Override
	public void start(Stage stage) throws Exception {
		INSTANCE = this;
		this.stage = stage;

		Base base = FxHelper.load(getMainFrameClass());

//        Date jvmLaunched = new Date(Long.parseLong(System.getProperty("__jvm_launched")));
//        Date appletLaunched = new Date(Long.parseLong(System.getProperty("__applet_launched")));
//        Date now = new Date(System.currentTimeMillis());
//
//        System.out.println("launch time: "+(now.getTime() - jvmLaunched.getTime()));

		loadStage();
		stage.setTitle(app.getName());
		base.show();

		stage.setOnCloseRequest(event -> saveStage());
	}

	Class<? extends Base> getMainFrameClass() {
		return ClientFrame.class;
	}

	protected void customize() {}

	public void saveStage() {
		try {
			StageCfg cfg = new StageCfg(stage());
			DumperOptions options = new DumperOptions();
			options.setPrettyFlow(true);
			Yaml yaml = new Yaml(options);
			String dump = yaml.dump(cfg);
			FileUtils.writeStringToFile(getFile(), dump);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadStage() {
		try {
			Yaml yaml = new Yaml();
			StageCfg cfg = (StageCfg) yaml.load(FileUtils.readFileToString(getFile()));
			stage().setX(cfg.getX());
			stage().setY(cfg.getY());
			stage().setWidth(cfg.getWidth());
			stage().setHeight(cfg.getHeight());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File getFile() {
		return Paths.get(
				client.getDirectory().getPath(),
				getClass().getSimpleName() + ".yaml").toFile();
	}

}