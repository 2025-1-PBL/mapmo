package com.pbl.mapmo.domain.common;

import com.pbl.mapmo.domain.brand.BrandDto;
import com.pbl.mapmo.domain.event.EventDto;
import com.pbl.mapmo.domain.franchise.FranchiseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    /**
     * 모든 브랜드 조회
     */
    @GetMapping
    public ResponseEntity<List<BrandDto.Response>> getAllBrands() {
        List<BrandDto.Response> brands = brandService.getAllBrands();
        return ResponseEntity.ok(brands);
    }

    /**
     * 브랜드 페이징 조회
     */
    @GetMapping("/page")
    public ResponseEntity<Page<BrandDto.Response>> getBrandsByPage(Pageable pageable) {
        Page<BrandDto.Response> brands = brandService.getBrandsByPage(pageable);
        return ResponseEntity.ok(brands);
    }

    /**
     * 브랜드명으로 브랜드 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<BrandDto.Response>> searchBrandsByName(@RequestParam String name) {
        List<BrandDto.Response> brands = brandService.searchBrandsByName(name);
        return ResponseEntity.ok(brands);
    }

    /**
     * 브랜드 상세 조회
     */
    @GetMapping("/{brandId}")
    public ResponseEntity<BrandDto.Response> getBrandById(@PathVariable Integer brandId) {
        BrandDto.Response brand = brandService.getBrandById(brandId);
        return ResponseEntity.ok(brand);
    }

    /**
     * 브랜드 생성
     */
    @PostMapping
    public ResponseEntity<BrandDto.Response> createBrand(@RequestBody BrandDto.Request brandDto) {
        BrandDto.Response createdBrand = brandService.createBrand(brandDto);
        return new ResponseEntity<>(createdBrand, HttpStatus.CREATED);
    }

    /**
     * 브랜드 정보 수정
     */
    @PutMapping("/{brandId}")
    public ResponseEntity<BrandDto.Response> updateBrand(@PathVariable Integer brandId, @RequestBody BrandDto.Request brandDto) {
        BrandDto.Response updatedBrand = brandService.updateBrand(brandId, brandDto);
        return ResponseEntity.ok(updatedBrand);
    }

    /**
     * 브랜드 삭제
     */
    @DeleteMapping("/{brandId}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Integer brandId) {
        brandService.deleteBrand(brandId);
        return ResponseEntity.noContent().build();
    }

    // 프랜차이즈 관련 API

    /**
     * 프랜차이즈 목록 조회 (브랜드별)
     */
    @GetMapping("/{brandId}/franchises")
    public ResponseEntity<List<FranchiseDto.Response>> getFranchisesByBrandId(@PathVariable Integer brandId) {
        List<FranchiseDto.Response> franchises = brandService.getFranchisesByBrandId(brandId);
        return ResponseEntity.ok(franchises);
    }

    /**
     * 프랜차이즈 생성
     */
    @PostMapping("/{brandId}/franchises")
    public ResponseEntity<FranchiseDto.Response> createFranchise(@PathVariable Integer brandId, @RequestBody FranchiseDto.Request franchiseDto) {
        FranchiseDto.Response createdFranchise = brandService.createFranchise(brandId, franchiseDto);
        return new ResponseEntity<>(createdFranchise, HttpStatus.CREATED);
    }

    /**
     * 프랜차이즈 상세 조회
     */
    @GetMapping("/franchises/{franchiseId}")
    public ResponseEntity<FranchiseDto.Response> getFranchiseById(@PathVariable Integer franchiseId) {
        FranchiseDto.Response franchise = brandService.getFranchiseById(franchiseId);
        return ResponseEntity.ok(franchise);
    }

    /**
     * 프랜차이즈 정보 수정
     */
    @PutMapping("/franchises/{franchiseId}")
    public ResponseEntity<FranchiseDto.Response> updateFranchise(@PathVariable Integer franchiseId, @RequestBody FranchiseDto.Request franchiseDto) {
        FranchiseDto.Response updatedFranchise = brandService.updateFranchise(franchiseId, franchiseDto);
        return ResponseEntity.ok(updatedFranchise);
    }

    /**
     * 프랜차이즈 삭제
     */
    @DeleteMapping("/franchises/{franchiseId}")
    public ResponseEntity<Void> deleteFranchise(@PathVariable Integer franchiseId) {
        brandService.deleteFranchise(franchiseId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 위치 기반 프랜차이즈 검색
     */
    @GetMapping("/franchises/nearby")
    public ResponseEntity<List<FranchiseDto.Response>> searchFranchisesByLocation(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(defaultValue = "1.0") Double distance) {
        List<FranchiseDto.Response> nearbyFranchises = brandService.searchFranchisesByLocation(lat, lng, distance);
        return ResponseEntity.ok(nearbyFranchises);
    }

    // 이벤트 관련 API

    /**
     * 이벤트 목록 조회 (브랜드별)
     */
    @GetMapping("/{brandId}/events")
    public ResponseEntity<List<EventDto.Response>> getEventsByBrandId(@PathVariable Integer brandId) {
        List<EventDto.Response> events = brandService.getEventsByBrandId(brandId);
        return ResponseEntity.ok(events);
    }

    /**
     * 이벤트 생성
     */
    @PostMapping("/{brandId}/events")
    public ResponseEntity<EventDto.Response> createEvent(@PathVariable Integer brandId, @RequestBody EventDto.Request eventDto) {
        EventDto.Response createdEvent = brandService.createEvent(brandId, eventDto);
        return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
    }

    /**
     * 이벤트 상세 조회
     */
    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventDto.Response> getEventById(@PathVariable Integer eventId) {
        EventDto.Response event = brandService.getEventById(eventId);
        return ResponseEntity.ok(event);
    }

    /**
     * 이벤트 정보 수정
     */
    @PutMapping("/events/{eventId}")
    public ResponseEntity<EventDto.Response> updateEvent(@PathVariable Integer eventId, @RequestBody EventDto.Request eventDto) {
        EventDto.Response updatedEvent = brandService.updateEvent(eventId, eventDto);
        return ResponseEntity.ok(updatedEvent);
    }

    /**
     * 이벤트 삭제
     */
    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Integer eventId) {
        brandService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }
}