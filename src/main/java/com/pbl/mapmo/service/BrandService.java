package com.pbl.mapmo.service;

import com.pbl.mapmo.dto.BrandDto;
import com.pbl.mapmo.dto.EventDto;
import com.pbl.mapmo.dto.FranchiseDto;
import com.pbl.mapmo.entity.Brand;
import com.pbl.mapmo.entity.Event;
import com.pbl.mapmo.entity.Franchise;
import com.pbl.mapmo.exception.ResourceNotFoundException;
import com.pbl.mapmo.repository.BrandRepository;
import com.pbl.mapmo.repository.EventRepository;
import com.pbl.mapmo.repository.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandService {

    @Value("${spring.security.client.kakao.client-id}")
    private String kakaoClientId;

    private final BrandRepository brandRepository;
    private final FranchiseRepository franchiseRepository;
    private final EventRepository eventRepository;

    /**
     * 모든 브랜드 조회
     */
    public List<BrandDto.Response> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 브랜드 페이징 조회
     */
    public Page<BrandDto.Response> getBrandsByPage(Pageable pageable) {
        return brandRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    /**
     * 브랜드명으로 브랜드 검색
     */
    public List<BrandDto.Response> searchBrandsByName(String name) {
        return brandRepository.findByNameContaining(name).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 브랜드 상세 조회
     */
    public BrandDto.Response getBrandById(Integer brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("브랜드를 찾을 수 없습니다. ID: " + brandId));
        return convertToDto(brand);
    }

    /**
     * 브랜드 생성
     */
    @Transactional
    public BrandDto.Response createBrand(BrandDto.Request brandDto) {
        Brand brand = Brand.builder()
                .name(brandDto.getName())
                .build();

        Brand savedBrand = brandRepository.save(brand);
        return convertToDto(savedBrand);
    }

    /**
     * 브랜드 정보 수정
     */
    @Transactional
    public BrandDto.Response updateBrand(Integer brandId, BrandDto.Request brandDto) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("브랜드를 찾을 수 없습니다. ID: " + brandId));

        brand.setName(brandDto.getName());
        Brand updatedBrand = brandRepository.save(brand);
        return convertToDto(updatedBrand);
    }

    /**
     * 브랜드 삭제
     */
    @Transactional
    public void deleteBrand(Integer brandId) {
        if (!brandRepository.existsById(brandId)) {
            throw new ResourceNotFoundException("브랜드를 찾을 수 없습니다. ID: " + brandId);
        }
        brandRepository.deleteById(brandId);
    }

    /**
     * 프랜차이즈 목록 조회 (브랜드별)
     */
    public List<FranchiseDto.Response> getFranchisesByBrandId(Integer brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("브랜드를 찾을 수 없습니다. ID: " + brandId));

        return brand.getFranchises().stream()
                .map(this::convertToFranchiseDto)
                .collect(Collectors.toList());
    }

    /**
     * 프랜차이즈 생성
     */
    @Transactional
    public FranchiseDto.Response createFranchise(Integer brandId, FranchiseDto.Request franchiseDto) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("브랜드를 찾을 수 없습니다. ID: " + brandId));

        Franchise franchise = Franchise.builder()
                .name(franchiseDto.getName())
                .location(franchiseDto.getLocation())
                .latitude(franchiseDto.getLatitude())
                .longitude(franchiseDto.getLongitude())
                .brand(brand)
                .build();

        Franchise savedFranchise = franchiseRepository.save(franchise);
        return convertToFranchiseDto(savedFranchise);
    }

    /**
     * 프랜차이즈 상세 조회
     */
    public FranchiseDto.Response getFranchiseById(Integer franchiseId) {
        Franchise franchise = franchiseRepository.findById(franchiseId)
                .orElseThrow(() -> new ResourceNotFoundException("프랜차이즈를 찾을 수 없습니다. ID: " + franchiseId));
        return convertToFranchiseDto(franchise);
    }

    /**
     * 프랜차이즈 정보 수정
     */
    @Transactional
    public FranchiseDto.Response updateFranchise(Integer franchiseId, FranchiseDto.Request franchiseDto) {
        Franchise franchise = franchiseRepository.findById(franchiseId)
                .orElseThrow(() -> new ResourceNotFoundException("프랜차이즈를 찾을 수 없습니다. ID: " + franchiseId));

        franchise.setName(franchiseDto.getName());
        franchise.setLocation(franchiseDto.getLocation());
        franchise.setLatitude(franchiseDto.getLatitude());
        franchise.setLongitude(franchiseDto.getLongitude());

        Franchise updatedFranchise = franchiseRepository.save(franchise);
        return convertToFranchiseDto(updatedFranchise);
    }

    /**
     * 프랜차이즈 삭제
     */
    @Transactional
    public void deleteFranchise(Integer franchiseId) {
        if (!franchiseRepository.existsById(franchiseId)) {
            throw new ResourceNotFoundException("프랜차이즈를 찾을 수 없습니다. ID: " + franchiseId);
        }
        franchiseRepository.deleteById(franchiseId);
    }

    /**
     * 위치 기반 프랜차이즈 검색
     */
    public List<FranchiseDto.Response> searchFranchisesByLocation(Double lat, Double lng, Double distance) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoClientId);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = String.format(
                "https://dapi.kakao.com/v2/local/search/category.json?category_group_code=CS2&page=1&size=15&sort=accuracy&x=%f&y=%f&radius=%d",
                lng, lat, distance.intValue()
        );

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println("Kakao API Response: " + response.getBody());

        List<Franchise> nearbyFranchises = franchiseRepository.findNearbyFranchises(lat, lng, distance);
        return nearbyFranchises.stream()
                .map(this::convertToFranchiseDto)
                .collect(Collectors.toList());
    }

    /**
     * 이벤트 목록 조회 (브랜드별)
     */
    public List<EventDto.Response> getEventsByBrandId(Integer brandId) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("브랜드를 찾을 수 없습니다. ID: " + brandId));

        return brand.getEvents().stream()
                .map(this::convertToEventDto)
                .collect(Collectors.toList());
    }

    /**
     * 이벤트 생성
     */
    @Transactional
    public EventDto.Response createEvent(Integer brandId, EventDto.Request eventDto) {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("브랜드를 찾을 수 없습니다. ID: " + brandId));

        Event event = Event.builder()
                .title(eventDto.getTitle())
                .content(eventDto.getContent())
                .url(eventDto.getUrl())
                .brand(brand)
                .build();

        Event savedEvent = eventRepository.save(event);
        return convertToEventDto(savedEvent);
    }

    /**
     * 이벤트 상세 조회
     */
    public EventDto.Response getEventById(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("이벤트를 찾을 수 없습니다. ID: " + eventId));
        return convertToEventDto(event);
    }

    /**
     * 이벤트 정보 수정
     */
    @Transactional
    public EventDto.Response updateEvent(Integer eventId, EventDto.Request eventDto) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("이벤트를 찾을 수 없습니다. ID: " + eventId));

        event.setTitle(eventDto.getTitle());
        event.setContent(eventDto.getContent());
        event.setUrl(eventDto.getUrl());

        Event updatedEvent = eventRepository.save(event);
        return convertToEventDto(updatedEvent);
    }

    /**
     * 이벤트 삭제
     */
    @Transactional
    public void deleteEvent(Integer eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("이벤트를 찾을 수 없습니다. ID: " + eventId);
        }
        eventRepository.deleteById(eventId);
    }

    /**
     * Brand 엔티티를 BrandDto.Response로 변환
     */
    private BrandDto.Response convertToDto(Brand brand) {
        return BrandDto.Response.of(brand);
    }

    /**
     * Franchise 엔티티를 FranchiseDto.Response로 변환
     */
    private FranchiseDto.Response convertToFranchiseDto(Franchise franchise) {
        return FranchiseDto.Response.of(franchise);
    }

    /**
     * Event 엔티티를 EventDto.Response로 변환
     */
    private EventDto.Response convertToEventDto(Event event) {
        return EventDto.Response.of(event);
    }
}