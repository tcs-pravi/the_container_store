package com.containerstore.prestonintegrations.proposal.cache;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/cache")
@RestController
public class CacheResource {

    private final CacheManager cacheManager;

    @DeleteMapping("/evict")
    public ResponseEntity<Object> evictAllCacheValues(@RequestParam("cache-alias") CacheRegion cacheRegion) {
        log.info("Rest Request to clear cached entries from {}", cacheRegion);
        cacheManager.getCache(cacheRegion.toLowerCaseString()).clear();
        return ResponseEntity.accepted().build();
    }
}
