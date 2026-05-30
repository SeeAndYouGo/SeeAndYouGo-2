package com.SeeAndYouGo.SeeAndYouGo.connection;

import com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider.ConnectionProvider;
import com.SeeAndYouGo.SeeAndYouGo.connection.connectionProvider.ConnectionProviderFactory;
import com.SeeAndYouGo.SeeAndYouGo.connection.dto.ConnectionVO;
import com.SeeAndYouGo.SeeAndYouGo.restaurant.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 테스트 목표: 각 식당의 혼잡도를 잘 가져오는지 검증.
 *
 * ConnectionService#getRecentConnection 의 동작을 검증한다.
 *  - 운영시간 내 정상 조회: 식당명을 파싱해 알맞은 ConnectionProvider 를 통해 ConnectionVO 를 반환한다.
 *  - 식당번호("2") 만으로도 조회 가능 (Restaurant.parseName).
 *  - 비운영시간: Provider 호출 없이 connected=-1 의 ConnectionVO 를 반환한다.
 *  - 잘못된 식당명: IllegalArgumentException 으로 거부된다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("식당 혼잡도 조회 - ConnectionService.getRecentConnection")
class ConnectionServiceTest {

    @Mock private ConnectionRepository connectionRepository;
    @Mock private ConnectionProviderFactory connectionProviderFactory;
    @Mock private ConnectionProvider connectionProvider;

    @InjectMocks private ConnectionService connectionService;

    @BeforeEach
    void setUp() {
        // 기본은 "항상 운영중"
        ReflectionTestUtils.setField(connectionService, "operatingStartTime", "00:00");
        ReflectionTestUtils.setField(connectionService, "operatingEndTime", "23:59:59");
    }

    @Test
    @DisplayName("운영시간 내에 식당명으로 조회하면 해당 식당의 ConnectionProvider 결과가 그대로 반환된다")
    void getRecentConnection_returnsProviderResult() throws Exception {
        // given
        ConnectionVO expected = new ConnectionVO(120, "2025-11-03 12:00:00", Restaurant.제2학생회관);
        given(connectionProviderFactory.getConnectionProvider(Restaurant.제2학생회관)).willReturn(connectionProvider);
        given(connectionProvider.getRecentConnection(Restaurant.제2학생회관)).willReturn(expected);

        // when
        ConnectionVO actual = connectionService.getRecentConnection("제2학생회관");

        // then
        assertThat(actual).isSameAs(expected);
        assertThat(actual.getConnected()).isEqualTo(120);
        assertThat(actual.getRestaurant()).isEqualTo(Restaurant.제2학생회관);
        assertThat(actual.getTime()).isEqualTo("2025-11-03 12:00:00");
    }

    @Test
    @DisplayName("식당 번호(\"2\") 만으로 조회해도 정상 처리된다")
    void getRecentConnection_acceptsNumericName() throws Exception {
        // given
        ConnectionVO expected = new ConnectionVO(50, "2025-11-03 09:30:00", Restaurant.제2학생회관);
        given(connectionProviderFactory.getConnectionProvider(Restaurant.제2학생회관)).willReturn(connectionProvider);
        given(connectionProvider.getRecentConnection(Restaurant.제2학생회관)).willReturn(expected);

        // when
        ConnectionVO actual = connectionService.getRecentConnection("2");

        // then
        assertThat(actual.getRestaurant()).isEqualTo(Restaurant.제2학생회관);
        assertThat(actual.getConnected()).isEqualTo(50);
    }

    @Test
    @DisplayName("상록회관(번호 4)도 적절한 Provider 를 통해 정상 조회된다")
    void getRecentConnection_sangrok() throws Exception {
        ConnectionVO expected = new ConnectionVO(80, "2025-11-03 11:45:00", Restaurant.상록회관);
        given(connectionProviderFactory.getConnectionProvider(Restaurant.상록회관)).willReturn(connectionProvider);
        given(connectionProvider.getRecentConnection(Restaurant.상록회관)).willReturn(expected);

        ConnectionVO actual = connectionService.getRecentConnection("상록회관");

        assertThat(actual.getRestaurant()).isEqualTo(Restaurant.상록회관);
        assertThat(actual.getConnected()).isEqualTo(80);
    }

    @Test
    @DisplayName("학생생활관(기숙사 식당)도 적절한 Provider 를 통해 정상 조회된다")
    void getRecentConnection_dormitory() throws Exception {
        ConnectionVO expected = new ConnectionVO(30, "2025-11-03 12:10:00", Restaurant.학생생활관);
        given(connectionProviderFactory.getConnectionProvider(Restaurant.학생생활관)).willReturn(connectionProvider);
        given(connectionProvider.getRecentConnection(Restaurant.학생생활관)).willReturn(expected);

        ConnectionVO actual = connectionService.getRecentConnection("학생생활관");

        assertThat(actual.getRestaurant()).isEqualTo(Restaurant.학생생활관);
        assertThat(actual.getConnected()).isEqualTo(30);
    }

    @Test
    @DisplayName("비운영시간에는 Provider 를 호출하지 않고 connected=-1 의 ConnectionVO 를 반환한다")
    void getRecentConnection_outsideOperatingHours_returnsMinusOne() throws Exception {
        // given - start=end=00:00 이면 모든 시각이 비운영시간
        ReflectionTestUtils.setField(connectionService, "operatingStartTime", "00:00");
        ReflectionTestUtils.setField(connectionService, "operatingEndTime", "00:00");

        // when
        ConnectionVO actual = connectionService.getRecentConnection("제2학생회관");

        // then
        assertThat(actual.getConnected()).isEqualTo(-1);
        assertThat(actual.getRestaurant()).isEqualTo(Restaurant.제2학생회관);
        assertThat(actual.getTime()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
        verifyNoInteractions(connectionProviderFactory, connectionProvider);
    }

    @Test
    @DisplayName("존재하지 않는 식당명은 IllegalArgumentException 으로 거부된다")
    void getRecentConnection_invalidRestaurant() {
        assertThatThrownBy(() -> connectionService.getRecentConnection("없는식당"))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(connectionProviderFactory, connectionProvider);
    }
}
