package br.com.guilda.registro.dto;

import java.util.List;

public record PageResult<T>(
    List<T> items,
    long totalCount,
    int page,
    int size,
    int totalPages
) {
}
