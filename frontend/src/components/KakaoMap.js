import { useEffect, useRef } from "react";
import styled from "@emotion/styled";

const MapContainer = styled.div`
	width: 100%;
	display: inline-block;
	margin-left: 5px;
	margin-right: 5px;
`;

const RestaurantName = [
  "1학생회관",
  "2학생회관",
  "3학생회관",
  "상록회관",
  "생활과학대학",
  "학생생활관",
]

const RestaurantLocation = [
  [36.367873, 127.343175], // 1학생회관
  [36.366034, 127.345821], // 2학생회관
  [36.371531, 127.344679], // 3학생회관
  [36.368539, 127.350378], // 상록회관
  [36.375791, 127.342973], // 생활과학대
  [36.372584, 127.346340], // 학생생활관
]

const RestaurantLocationId = [34354676, 17561993, 2055243707, 17567426, 10834942, 19083045]
// 제1학생회관, 인재개발원, 제3학생회관, 상록회관, 생활과학대학, 학생생활관11동

const KakaoMap = ({ restaurantId, modalOpen }) => {
  const mapRef = useRef(null);
  const markerRef = useRef(null);
  const mapInstanceRef = useRef(null);

  useEffect(() => {
    const kakaoMapKey = process.env.REACT_APP_KAKAOMAP_KEY;
    const scriptId = "kakao-map-script";

    const initializeMap = () => {
      const [lat, lng] = RestaurantLocation[restaurantId - 1];
      const nowRestaurantId = RestaurantLocationId[restaurantId - 1];
      const center = new window.kakao.maps.LatLng(lat, lng);

      const map = new window.kakao.maps.Map(mapRef.current, {
        center,
        level: 3,
        draggable: true,
      });

      const marker = new window.kakao.maps.Marker({
        position: center,
        map,
      });

      const content =
				'<div class="overlayWrapper" style="position: relative; top: 30px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); background: white; transition: all 0.3s ease;">' +
        `  <a href="https://map.kakao.com/link/map/${nowRestaurantId}" target="_blank" style="text-decoration: none; color: inherit;">` +
        '    <div class="overlayBox" style="display: flex; align-items: center; justify-content: center;">' +
				`      <span class="overlayTitle" style="font-size: 14px; font-weight: 600; color: #333; padding: 8px 16px; border-radius: 8px;">${RestaurantName[restaurantId - 1]}</span>` +
        `      <span class="material-symbols-outlined" style="border-radius: 0px 12px 12px 0px; color: #fff; background-color: #333; padding: 8px 5px">chevron_right</span>` +
        "    </div>" +
				"  </a>" +
        '  <div style="position: absolute; top: 100%; left: 50%; margin-left: -8px; width: 0; height: 0; border-left: 8px solid transparent; border-right: 8px solid transparent; border-top: 8px solid white;"></div>' +
				"</div>";

      const customOverlay = new window.kakao.maps.CustomOverlay({
        map: map,
        position: center,
        content,
        yAnchor: 3,
      });

      // 마커에 클릭 이벤트를 추가하여 카카오맵으로 이동
      window.kakao.maps.event.addListener(marker, "click", () => {
        const url = `https://map.kakao.com/?itemId=${nowRestaurantId}`;
        window.open(url);
      });

      map.setDraggable(true);
      mapInstanceRef.current = map;
      markerRef.current = marker;

      setTimeout(() => {
        map.relayout();
        map.setCenter(center);
      }, 300);

    };

    if (document.getElementById(scriptId)) {
      if (window.kakao && window.kakao.maps) {
        initializeMap();
      }
      return;
    }

    const script = document.createElement("script");
    script.id = scriptId;
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${kakaoMapKey}&autoload=false&libraries=services,clusterer,drawing`;
    script.async = true;
    script.onload = () => {
      window.kakao.maps.load(() => {
        initializeMap();
      });
    };
    document.head.appendChild(script);
  }, [restaurantId]);

  useEffect(() => {
    if (!mapInstanceRef.current || !markerRef.current) return;

    const observer = new ResizeObserver(() => {
      mapInstanceRef.current.relayout();
      const [lat, lng] = RestaurantLocation[restaurantId - 1];

      const newCenter = new window.kakao.maps.LatLng(lat, lng);
      mapInstanceRef.current.setCenter(newCenter);
      markerRef.current.setPosition(newCenter);
    });

    observer.observe(mapRef.current);

    setTimeout(() => {
      mapInstanceRef.current.relayout();
    }, 300);

    return () => {
      observer.disconnect();
    };
  }, [restaurantId, modalOpen]);


	return (
		<MapContainer>
			<div ref={mapRef} style={{ width: "270px", height: "300px" }}></div>
		</MapContainer>
	);
};

export default KakaoMap;
