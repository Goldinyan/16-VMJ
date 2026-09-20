package parsing;

import util.Logger;

public class ParseResult {
    private ParseError err;
    private int data;
    private boolean success;
    private int curLine;

    public ParseResult() {
        this.err = null;
        this.data = 0;
        this.success = false;
        this.curLine = 0;
    }

    public void setData(int data) {
        this.data = data;
        this.success = true;
        this.err = null;
        this.curLine = 0;
    }

    public void setError(ParseError result, int curLine) {
        this.data = 0;
        this.success = false;
        this.err = result;
        this.curLine = curLine;
    }

    public boolean isOk() {
        return this.success;
    }

    public int getData() {
        return this.data;
    }

    public void logError() {
        Logger.logParseError(err, curLine);
    }
}