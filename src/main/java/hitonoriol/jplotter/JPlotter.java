package hitonoriol.jplotter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.json.JSONArray;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class JPlotter extends Application {

	private NumberAxis xAxis = new NumberAxis(), yAxis = new NumberAxis();
	private LineChart<Number, Number> plot = new LineChart<Number, Number>(xAxis, yAxis);
	private StackPane plotContainer = new StackPane(plot);

	@Override
	public void start(Stage primaryStage) throws Exception {
		Scene scene = new Scene(plotContainer);
		Util.loadStyles(scene);
		primaryStage.setScene(scene);
		primaryStage.setTitle("JPlotter");

		if (getParameters().getRaw().isEmpty()) {
			FileChooser chooser = new FileChooser();
			chooser.setInitialDirectory(new File("."));
			chooser.setTitle("Choose point list to plot");
			chooser.getExtensionFilters().add(new ExtensionFilter("JSON point lists", "*.json"));
			File ptFile = chooser.showOpenDialog(primaryStage);
			if (ptFile == null)
				Platform.exit();
			plotJSON(Files.readString(ptFile.toPath()));
		}

		plot.setTitle("");
		Arrays.asList(xAxis, yAxis).forEach(axis -> axis.tickLabelFontProperty().set(Font.font(14)));
		initControls();

		primaryStage.show();
	}

	private void initControls() {
		/* Pan with LMB / RMB */
		ChartPanManager panner = new ChartPanManager(plot);
		panner.setMouseFilter(mouseEvent -> {
			MouseButton btn = mouseEvent.getButton();
			if (btn != MouseButton.PRIMARY && btn != MouseButton.SECONDARY)
				mouseEvent.consume();
		});
		panner.start();
		/* Zoom with mouse wheel */
		JFXChartUtil.setupZooming(plot, mouseEvent -> mouseEvent.consume());
		/* Stop the scroll event from propagating back to tab root when zooming */
		plotContainer.addEventHandler(ScrollEvent.ANY, event -> event.consume());
		/* Double click to reset zoom & position */
		JFXChartUtil.addDoublePrimaryClickAutoRangeHandler(plot);
	}

	private void plotArgs() {
		plotJSON(getParameters().getRaw().stream().collect(Collectors.joining()));
	}

	private void plotJSON(String pointsJson) {
		List<List<Point>> pointLists = new ArrayList<>();
		Deque<String> names = new ArrayDeque<>();
		JSONArray ptArr = new JSONArray(pointsJson);

		for (int i = 0; i < ptArr.length(); ++i) {
			JSONArray entry = ptArr.getJSONArray(i);
			List<Point> points = new ArrayList<>();
			names.add(entry.getString(0));
			JSONArray xArr = entry.getJSONArray(1);
			JSONArray yArr = entry.getJSONArray(2);
			for (int j = 0; j < xArr.length(); ++j)
				points.add(new Point(xArr.getNumber(j), yArr.getNumber(j)));
			pointLists.add(points);
		}

		pointLists.forEach(list -> {
			XYChart.Series<Number, Number> series = new XYChart.Series<>();
			ObservableList<Data<Number, Number>> data = series.getData();
			list.forEach(point -> {
				data.add(new Data<Number, Number>(point.x, point.y));
			});
			series.setName(names.pop());
			plot.getData().add(series);
		});
	}

	public static void run(String[] args) {
		launch(args);
	}

	private static class Point {
		Number x, y;

		public Point(Number x, Number y) {
			this.x = x;
			this.y = y;
		}
	}
}
