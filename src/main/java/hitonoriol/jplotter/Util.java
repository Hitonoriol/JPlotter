package hitonoriol.jplotter;

import javafx.scene.Scene;

public class Util {
	public static void loadStyles(Scene scene) {
		scene.getStylesheets().add(Util.class.getResource("/styles.css").toExternalForm());
	}
}
