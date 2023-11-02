package com.dstolis.employeeapi.model.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ErrorResponseDTO(OffsetDateTime timestamp, String message, UUID errorId, String path) {
}
