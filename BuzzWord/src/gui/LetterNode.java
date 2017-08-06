package gui;

import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Po Yiu Ho
 */
public class LetterNode extends Region {
    Label node;
    LetterNode connectedTo;
    char c;
    int row;
    int col;
    boolean used;
    boolean connected;

    public LetterNode(int r, int c) {
        this.node = generateLabel();
        node.setId("letterNode");
        node.prefWidthProperty().set(50);
        node.prefHeightProperty().set(50);
        node.setAlignment(Pos.CENTER);
        this.getChildren().setAll(node);
        this.used = false;
        this.connected = false;
        row = r;
        col = c;
    }

    private Label generateLabel() {
        char[] letters = {
                'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A',
                'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E',
                'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I', 'I',
                'O', 'O', 'O', 'O', 'O', 'O', 'O', 'O',
                'U', 'U', 'U', 'U',
                'T', 'T', 'T', 'T', 'T', 'T',
                'N', 'N', 'N', 'N', 'N', 'N',
                'S', 'S', 'S', 'S',
                'H', 'H',
                'R', 'R', 'R', 'R', 'R', 'R',
                'D', 'D', 'D', 'D',
                'L', 'L', 'L', 'L',
                'B', 'B',
                'C', 'C',
                'F', 'F',
                'G', 'G', 'G',
                'J',
                'K',
                'M', 'M',
                'P', 'P',
                'Q',
                'V', 'V',
                'W', 'W',
                'X',
                'Y', 'Y',
                'Z'};

        int common = ThreadLocalRandom.current().nextInt(0, letters.length);
        c = letters[common];
        Label n = new Label(Character.toString(c));
        return n;

    }

    public boolean getUsed() {return this.used;}

    public void used() {this.used = true;}
    public void cancel() {this.used = false;}

    public int getRow() {return this.row;}

    public int getCol() {return this.col;}

    public char getChar() {return this.c;}

    public Label getLabel() {return this.node;}

    public void setRow(int r) {this.row = r;}

    public void setCol(int c) {this.col = c;}

    public void setNodeId(String s) {this.node.setId(s);}

    public boolean isRowLeaf() {
        if (this.row == 5 || this.row == 1) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isColLeaf() {
        if (this.col == 5 || this.col == 1) {
            return true;
        } else {
            return false;
        }
    }

    public void setChar(char ch) {
        this.c = ch;
        this.node.setText(Character.toString(ch));
    }

    public Line connectTo(LetterNode next) {
        Line line = new Line();
        line.setStartX(this.col * this.widthProperty().doubleValue() - this.widthProperty().divide(2).doubleValue() + 50 + (25 * (this.col - 1)));
        line.setStartY(this.row * this.heightProperty().doubleValue() - this.heightProperty().divide(2).doubleValue() + 20 + (25 * (this.row - 1)));
        line.setEndX(next.getCol() * next.widthProperty().doubleValue() - next.widthProperty().divide(2).doubleValue() + 50 + (25 * (next.getCol() - 1)));
        line.setEndY(next.getRow() * next.heightProperty().doubleValue() - next.heightProperty().divide(2).doubleValue() + 20 + (25 * (next.getRow() - 1)));
        line.setStroke(Paint.valueOf("#fff"));
        line.setStrokeWidth(2);

        this.connectedTo = next;
        this.connected = true;
        return line;
    }

    public void setConnected() {this.connected = true; }

    public boolean getConnected() {return this.connected;}

    public void highlight() {
        this.node.setId("dragNode");
        this.used = true;
    }

    public void reset() {
        this.node.setId("letterNode");
        this.used = false;
        this.connected = false;
    }
}
