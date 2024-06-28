package com.containerstore.prestonintegrations.proposal.store.dto;

import java.util.List;

public record PaginatedAPIResponse<T> (int page, int size, int totalPages, long totalElements, List<T> content){}
