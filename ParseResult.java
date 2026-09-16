public enum ParseResult {
    SUCCESS,
    SYNTAX_ERROR,      // Wrong format or missing statements, etc 
    INVALID_REGISTER,  // register id not within 0 to 15 
    INVALID_IMMEDIATE, // Number to big to fit into byte or short 
    SYMBOL_NOT_FOUND,  // like if a label doesnt exist in symbol table or sum 
    GENERIC_ERROR     
}

/*
int regId1 = getRegId(parts[1]);
int regId2 = getRegId(parts[2]);
                
if (regId1 == -1 || regId2 == -1) {
    return null;
}

ParseReturn regID1Result = getRegId(parts[i]);
int regID1 = regID1Result.getResult()
// soll irgendwie unpacken ob es value gibt oder nicht
*/

public class <T> ParseReturn {
    public ParseResult result;
    public T value;

    public boolean isOk() {
        return result == ParseResult.SUCCESS;
    }

    public ParseResult
        

}
