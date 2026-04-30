package com.isi.gestion_formation.dto;

import lombok.Data;
import java.util.List;

@Data
public class PropositionRequest {
    private List<Long> participantIds;
}