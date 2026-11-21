package model;
public enum Visibility {
    PUBLIC("PUBLIC"), PRIVATE("PRIVATE");
    private final String label;
    Visibility(String label){ this.label = label; }
    public String getLabel(){ return label; }
    public static Visibility fromString(String s){
        return "private".equalsIgnoreCase(s) ? PRIVATE : PUBLIC;
    }
}