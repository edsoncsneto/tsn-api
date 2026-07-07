package br.edu.ifpe.dnc.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.networkcalculus.dnc.ethernet.tsn.dto.DeviceAndInterfaceDTO;
import org.networkcalculus.dnc.ethernet.tsn.dto.EthernetLinkDTO;
import org.networkcalculus.dnc.ethernet.tsn.dto.EthernetNetworkDTO;

public class NetworkJsonParser {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DeviceAndInterfaceDTO.class, new DeviceAndInterfaceDTO.DeviceAndInterfaceDTOTypeAdapter())
            .registerTypeAdapter(EthernetLinkDTO.class, new EthernetLinkDTO.EthernetLinkDTOTypeAdapter())
            .create();

    public static EthernetNetworkDTO parse(String jsonContent) {
        return gson.fromJson(jsonContent, EthernetNetworkDTO.class);
    }
}
