package com.OmObe.OmO.Place.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class PlaceRecent {
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request{
        private List<String> placeNameList;
        private List<Long> placeIdList;
    }
}
