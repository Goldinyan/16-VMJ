package parsing;

public enum ParseError {
    SYNTAX_ERROR,      // Wrong format or missing statements, etc 
    INVALID_REGISTER,  // register id not within 0 to 15 
    INVALID_IMMEDIATE, // Number to big to fit into byte or short 
    SYMBOL_NOT_FOUND,  // like if a label doesnt exist in symbol table or sum 
    GENERIC_ERROR     
}



