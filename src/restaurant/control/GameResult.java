package restaurant.control;

import java.util.List;

/** Victory or Defeat, with one line per target explaining whether it was met */
public record GameResult(boolean victory, List<String> targetLines) {

    public GameResult {
        targetLines = List.copyOf(targetLines);
    }
}
