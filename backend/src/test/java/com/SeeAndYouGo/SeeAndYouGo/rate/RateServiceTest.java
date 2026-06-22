package com.SeeAndYouGo.SeeAndYouGo.rate;

import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateServiceTest {

    @Mock
    private RateRepository rateRepository;

    @Test
    @DisplayName("1학생회관 메뉴 캐시는 여러 번 초기화해도 중복 누적되지 않는다")
    void setRestaurant1MenuFieldIsIdempotent() {
        RateService rateService = new RateService(rateRepository);

        rateService.setRestaurant1MenuField();
        Map<String, Integer> firstSnapshot = getCategorySizes(rateService);

        rateService.setRestaurant1MenuField();
        Map<String, Integer> secondSnapshot = getCategorySizes(rateService);

        assertThat(secondSnapshot).isEqualTo(firstSnapshot);
    }

    @Test
    @DisplayName("평점 저장용 동기화는 메뉴 캐시를 불리지 않는다")
    void saveRateDoesNotGrowRestaurant1MenuCache() {
        RateService rateService = new RateService(rateRepository);
        when(rateRepository.existsByRestaurantAndDept(eq(Restaurant.제1학생회관), anyString())).thenReturn(true);

        rateService.setRestaurant1MenuField();
        Map<String, Integer> beforeSaveRate = getCategorySizes(rateService);

        rateService.saveRate();
        Map<String, Integer> afterSaveRate = getCategorySizes(rateService);

        assertThat(afterSaveRate).isEqualTo(beforeSaveRate);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> getCategorySizes(RateService rateService) {
        Map<String, List<String>> categories = (Map<String, List<String>>) ReflectionTestUtils.getField(
                rateService,
                "restaurant1MenuByCategory"
        );

        Map<String, Integer> snapshot = new LinkedHashMap<>();
        assertThat(categories).isNotNull();

        for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
            snapshot.put(entry.getKey(), entry.getValue().size());
        }

        return snapshot;
    }
}
