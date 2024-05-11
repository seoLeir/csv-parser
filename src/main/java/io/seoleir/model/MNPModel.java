package io.seoleir.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MNPModel {

    private String number;

    private String owner;

    private String rn;

    private String mnc;

    private String portDate;

    private String rowCount;
}
