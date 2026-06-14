package gruppo05.gtwshared.controller;

public interface LoginManager {
    void validateInfo(String username, String password);
    void setOnFailureCallback(Runnable r);
    void setOnSuccessCallback(Runnable r);
}