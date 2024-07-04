package net.javacrumbs.jsonunit.providers;

import com.google.gson.Gson;

public interface GsonMapperProvider extends MapperProvider {
    Gson getGson();
}
