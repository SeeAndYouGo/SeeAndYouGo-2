# SeeAndYouGo Backend 리팩토링 우선순위 분석

> **분석일**: 2025-11-01
> **분석 대상**: Spring Boot Backend (Java 11)
> **분석 범위**: Service, Controller, Repository, Entity 레이어

---

## 목차
1. [개요](#개요)
2. [심각도별 분류](#심각도별-분류)
3. [상세 분석](#상세-분석)
4. [리팩토링 로드맵](#리팩토링-로드맵)
5. [해결 완료](#-해결-완료)

---

## 개요

이 문서는 SeeAndYouGo 백엔드 코드베이스의 비효율적인 로직과 개선이 필요한 부분을 심층 분석한 결과입니다. 성능 문제, 보안 취약점, 코드 품질 문제를 중심으로 우선순위를 매겨 정리했습니다.

### 분석 결과 요약
- **치명적(Critical)**: 1건 - 로직 버그
- **높음(High)**: 11건 - N+1 쿼리, 심각한 성능 이슈
- **중간(Medium)**: 15건 - 코드 중복, 설계 개선
- **낮음(Low)**: 8건 - 마이너 개선 사항
- **해결완료**: 3건 - 평균 계산 오류, Path Traversal 보안 취약점, 비효율적인 정렬

---

## 심각도별 분류

### 🔴 Critical (치명적) - 즉시 수정 필요

#### 1. **로직 버그: 조기 return으로 인한 불완전한 데이터 저장**
- **위치**: `ConnectionService.java:45-73`
- **문제**:
```java
@Transactional
public void saveRecentConnection() throws Exception {
    for (Restaurant restaurant : Restaurant.values()) {
        if(connectionRepository.countByRestaurant(restaurant) > 0){
            String recentTime = connectionRepository.findTopByRestaurantOrderByTimeDesc(restaurant).getTime();
            if(haveRecentConnection(recentTime)){
                // 최신 데이터가 있다면 저장하지 않아도 됨.
                return;  // ❌ 버그: 첫 번째 레스토랑에서 return하면 나머지는 저장 안됨!
            }
        }
        // ... 저장 로직
    }
}
```
- **위험도**: ⚠️ **높음**
- **상세**:
  - 루프 내부에서 `return`을 사용하여 **모든 레스토랑 처리가 중단됨**
  - 예: 2학생회관에 최신 데이터가 있으면, 3학, 4학, 5학의 데이터는 저장되지 않음
  - 데이터 불일치 발생
- **영향**: WiFi 혼잡도 데이터의 불완전성
- **해결방안**:
```java
@Transactional
public void saveRecentConnection() throws Exception {
    for (Restaurant restaurant : Restaurant.values()) {
        if(connectionRepository.countByRestaurant(restaurant) > 0){
            String recentTime = connectionRepository.findTopByRestaurantOrderByTimeDesc(restaurant).getTime();
            if(haveRecentConnection(recentTime)){
                continue;  // ✅ 이 레스토랑만 건너뛰고 다음으로
            }
        }

        // 최신 데이터가 없다면 저장한다.
        ConnectionProvider connectionProvider = connectionProviderFactory.getConnectionProvider(restaurant);
        connectionProvider.updateConnectionMap(restaurant);
        ConnectionVO recentConnection = connectionProvider.getRecentConnection(restaurant);
        if (recentConnection == null) {
            continue;  // ✅ null이면 이 레스토랑만 건너뛰기
        }
        Connection connection = Connection.builder()
                                    .connected(recentConnection.getConnected())
                                    .time(recentConnection.getTime())
                                    .restaurant(recentConnection.getRestaurant())
                                    .build();

        connectionRepository.save(connection);
    }
}
```

---

### 🟠 High (높음) - 성능에 심각한 영향

#### 3. **N+1 쿼리: DishService.updateMainDish()**
- **위치**: `DishService.java:29-50`
- **문제**:
```java
@Transactional
public void updateMainDish(List<MainDishRequestDto> mainDishRequestDtos) {
    for (MainDishRequestDto mainDishRequestDto : mainDishRequestDtos) {
        List<String> mainDishNames = mainDishRequestDto.getMainDishList();

        for (String mainDishName : mainDishNames) {  // ❌ N+1 문제
            Optional<Dish> dish = dishRepository.findByName(mainDishName);
            if (dish.isPresent()) {
                dish.get().updateMainDish();
            }
        }

        for (String sideDishName : mainDishRequestDto.getSideDishList()) {  // ❌ N+1 문제
            Dish sideDish = dishRepository.findByName(sideDishName)
                .orElseThrow(EntityNotFoundException::new);
            sideDish.updateSideDish();
        }
    }
}
```
- **성능 영향**:
  - 요청 1개당 메인 요리 10개 + 반찬 5개 = **15번의 DB 쿼리**
  - 100개 요리 업데이트 시 **100번의 쿼리** 발생
- **해결방안**:
```java
@Transactional
public void updateMainDish(List<MainDishRequestDto> mainDishRequestDtos) {
    // 1. 모든 요리명 수집
    Set<String> allDishNames = new HashSet<>();
    for (MainDishRequestDto dto : mainDishRequestDtos) {
        allDishNames.addAll(dto.getMainDishList());
        allDishNames.addAll(dto.getSideDishList());
    }

    // 2. 한 번에 조회 (1번의 쿼리)
    List<Dish> dishes = dishRepository.findByNameIn(allDishNames);
    Map<String, Dish> dishMap = dishes.stream()
        .collect(Collectors.toMap(Dish::getName, Function.identity()));

    // 3. 업데이트
    for (MainDishRequestDto dto : mainDishRequestDtos) {
        for (String mainDishName : dto.getMainDishList()) {
            Dish dish = dishMap.get(mainDishName);
            if (dish == null) {
                throw new EntityNotFoundException(mainDishName + "에 해당하는 dish를 찾을 수 없습니다.");
            }
            dish.updateMainDish();
        }

        for (String sideDishName : dto.getSideDishList()) {
            Dish dish = dishMap.get(sideDishName);
            if (dish == null) {
                throw new EntityNotFoundException(sideDishName + "에 해당하는 dish를 찾을 수 없습니다.");
            }
            dish.updateSideDish();
        }
    }
}
```
- **필요한 Repository 메서드 추가**:
```java
// DishRepository에 추가
List<Dish> findByNameIn(Collection<String> names);
```

#### 5. **N+1 쿼리: MenuService.saveWeeklyMenu()**
- **위치**: `MenuService.java:287-321`
- **문제**:
```java
for (MenuVO menuVO : weeklyMenu) {
    List<DishVO> dishVOs = menuVO.getDishVOs();
    Menu menu = new Menu(menuVO);

    for (DishVO dishVO : dishVOs) {
        Dish dish;
        // ❌ 각 Dish마다 개별 조회
        if(!dishRepository.existsByName(dishVO.getName())){
            dish = Dish.builder()
                    .name(dishVO.getName())
                    .dishType(dishVO.getDishType())
                    .build();
            dishRepository.save(dish);
        }else{
            dish = dishRepository.findByName(dishVO.getName()).get();
        }
        menu.addDish(dish);
    }
    menuRepository.save(menu);
}
```
- **성능 영향**:
  - 주간 메뉴: 7일 × 5개 레스토랑 × 10개 요리 = **최대 350번의 쿼리**
- **해결방안**:
```java
// 1. 모든 요리명 수집
Set<String> allDishNames = weeklyMenu.stream()
    .flatMap(menuVO -> menuVO.getDishVOs().stream())
    .map(DishVO::getName)
    .collect(Collectors.toSet());

// 2. 기존 요리 한 번에 조회 (1번의 쿼리)
List<Dish> existingDishes = dishRepository.findByNameIn(allDishNames);
Map<String, Dish> dishMap = existingDishes.stream()
    .collect(Collectors.toMap(Dish::getName, Function.identity()));

// 3. 새로운 요리만 수집해서 일괄 저장
List<Dish> newDishes = new ArrayList<>();
for (MenuVO menuVO : weeklyMenu) {
    for (DishVO dishVO : menuVO.getDishVOs()) {
        if (!dishMap.containsKey(dishVO.getName())) {
            Dish newDish = Dish.builder()
                .name(dishVO.getName())
                .dishType(dishVO.getDishType())
                .build();
            newDishes.add(newDish);
            dishMap.put(dishVO.getName(), newDish);
        }
    }
}
dishRepository.saveAll(newDishes);  // 일괄 저장 (1번의 쿼리)

// 4. 메뉴 생성
for (MenuVO menuVO : weeklyMenu) {
    Menu menu = new Menu(menuVO);
    for (DishVO dishVO : menuVO.getDishVOs()) {
        Dish dish = dishMap.get(dishVO.getName());
        menu.addDish(dish);
    }
    menuRepository.save(menu);
}
```

#### 6. **N+1 쿼리: ReviewService.findRestaurantReviews()**
- **위치**: `ReviewService.java:125-142`
- **문제**:
```java
List<Menu> menus = menuRepository.findByRestaurantAndDate(restaurant, date);

List<Menu> param = new ArrayList<>();
// ❌ 각 메뉴마다 개별 조회
for (Menu menu : menus) {
    param.addAll(menuService.findAllMenuByMainDish(menu));  // N+1 발생
}
return reviewRepository.findByRestaurantAndMenuIn(restaurant, param);
```
- **성능 영향**:
  - 메뉴 10개일 때 **10번의 추가 쿼리** 발생
- **해결방안**:
```java
// MenuRepository에 메서드 추가
@Query("SELECT DISTINCT m FROM Menu m " +
       "JOIN m.menuDishes md " +
       "WHERE m.restaurant = :restaurant " +
       "AND md.dish IN (" +
       "  SELECT md2.dish FROM Menu m2 " +
       "  JOIN m2.menuDishes md2 " +
       "  WHERE m2.restaurant = :restaurant " +
       "  AND m2.date = :date " +
       "  AND md2.dish.dishType = 'MAIN'" +
       ")")
List<Menu> findMenusByRestaurantAndMainDishesInDate(
    @Param("restaurant") Restaurant restaurant,
    @Param("date") String date
);

// ReviewService에서 사용
public List<Review> findRestaurantReviews(String restaurantName, String date) {
    Restaurant restaurant = Restaurant.valueOf(Restaurant.parseName(restaurantName));

    if(restaurant.equals(Restaurant.제1학생회관)){
        return reviewRepository.findByRestaurant(restaurant);
    }

    // 한 번의 쿼리로 해결
    List<Menu> menus = menuRepository.findMenusByRestaurantAndMainDishesInDate(restaurant, date);
    return reviewRepository.findByRestaurantAndMenuIn(restaurant, menus);
}
```

#### 7. **다중 쿼리: MenuService.getOneWeekRestaurantMenu()**
- **위치**: `MenuService.java:88-102`
- **문제**:
```java
public List<Menu>[] getOneWeekRestaurantMenu(String restaurantName, String date) {
    LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    LocalDate startOfWeek = parsedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate endOfWeek = parsedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    List<Menu>[] weekMenuList = new List[7];

    int idx = -1;
    for(LocalDate i = startOfWeek; i.compareTo(endOfWeek) <= 0; i = i.plusDays(1)){
        weekMenuList[++idx] = getOneDayRestaurantMenu(restaurantName, i.toString());  // ❌ 7번의 쿼리
    }
    return weekMenuList;
}
```
- **성능 영향**:
  - **매번 7번의 DB 쿼리**
- **해결방안**:
```java
public List<Menu>[] getOneWeekRestaurantMenu(String restaurantName, String date) {
    LocalDate parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    LocalDate startOfWeek = parsedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    LocalDate endOfWeek = parsedDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

    String parseRestaurantName = Restaurant.parseName(restaurantName);
    Restaurant restaurant = Restaurant.valueOf(parseRestaurantName);

    // ✅ 한 번의 쿼리로 모든 메뉴 조회
    List<Menu> allMenus = menuRepository.findByRestaurantAndDateBetween(
        restaurant,
        startOfWeek.toString(),
        endOfWeek.toString()
    );

    // 날짜별로 그룹핑
    Map<String, List<Menu>> menusByDate = allMenus.stream()
        .collect(Collectors.groupingBy(Menu::getDate));

    List<Menu>[] weekMenuList = new List[7];
    int idx = 0;
    for(LocalDate i = startOfWeek; i.compareTo(endOfWeek) <= 0; i = i.plusDays(1)){
        weekMenuList[idx++] = sortMainDish(
            menusByDate.getOrDefault(i.toString(), new ArrayList<>())
        );
    }

    return weekMenuList;
}
```
- **필요한 Repository 메서드 추가**:
```java
// MenuRepository에 추가
@Query("SELECT m FROM Menu m WHERE m.restaurant = :restaurant " +
       "AND m.date >= :startDate AND m.date <= :endDate")
List<Menu> findByRestaurantAndDateBetween(
    @Param("restaurant") Restaurant restaurant,
    @Param("startDate") String startDate,
    @Param("endDate") String endDate
);
```

#### 8. **다중 쿼리: MenuService.checkWeekMenu()**
- **위치**: `MenuService.java:105-114`
- **문제**:
```java
@Transactional
public void checkWeekMenu(LocalDate monday, LocalDate sunday) {
    for(LocalDate date = monday; !date.isAfter(sunday); date = date.plusDays(1)){
        for (Restaurant restaurant : Restaurant.values()) {
            if(restaurant.equals(Restaurant.제1학생회관)) continue;

            checkMenuByDate(restaurant, date.toString());  // ❌ 7일 × 4개 = 28번 쿼리
        }
    }
}
```
- **성능 영향**:
  - **28번의 DB 쿼리 + 각각의 메뉴 생성 쿼리**
- **해결방안**:
```java
@Transactional
public void checkWeekMenu(LocalDate monday, LocalDate sunday) {
    // ✅ 한 번의 쿼리로 모든 메뉴 조회
    List<Menu> existingMenus = menuRepository.findByRestaurantNotAndDateBetween(
        Restaurant.제1학생회관,
        monday.toString(),
        sunday.toString()
    );

    // 존재하는 메뉴를 Map으로 변환 (Restaurant, Date, Dept, MenuType) -> Menu
    Map<String, Menu> menuMap = existingMenus.stream()
        .collect(Collectors.toMap(
            menu -> getMenuKey(menu.getRestaurant(), menu.getDate(), menu.getDept(), menu.getMenuType()),
            Function.identity()
        ));

    List<Menu> menusToCreate = new ArrayList<>();

    for(LocalDate date = monday; !date.isAfter(sunday); date = date.plusDays(1)){
        for (Restaurant restaurant : Restaurant.values()) {
            if(restaurant.equals(Restaurant.제1학생회관)) continue;

            // 필요한 메뉴 조합 체크
            checkAndAddMissingMenus(restaurant, date.toString(), menuMap, menusToCreate);
        }
    }

    // ✅ 일괄 저장
    if (!menusToCreate.isEmpty()) {
        menuRepository.saveAll(menusToCreate);
    }
}

private String getMenuKey(Restaurant restaurant, String date, Dept dept, MenuType menuType) {
    return restaurant + "_" + date + "_" + dept + "_" + menuType;
}

private void checkAndAddMissingMenus(Restaurant restaurant, String date,
                                     Map<String, Menu> menuMap,
                                     List<Menu> menusToCreate) {
    for (MenuType menuType : MenuType.values()) {
        List<Dept> deptsToCheck = getDeptsToCheck(restaurant, menuType);

        for (Dept dept : deptsToCheck) {
            String key = getMenuKey(restaurant, date, dept, menuType);
            if (!menuMap.containsKey(key)) {
                Dish defaultDish = getDefaultDish();
                Menu menu = Menu.builder()
                    .price(0)
                    .date(date)
                    .dept(dept)
                    .isOpen(false)
                    .menuType(menuType)
                    .restaurant(restaurant)
                    .build();
                menu.addDish(defaultDish);
                menusToCreate.add(menu);
            }
        }
    }
}

private List<Dept> getDeptsToCheck(Restaurant restaurant, MenuType menuType) {
    // fillMenu의 복잡한 로직을 깔끔하게 정리
    if (menuType == MenuType.BREAKFAST) {
        return restaurant == Restaurant.제2학생회관
            ? List.of(Dept.STUDENT)
            : Collections.emptyList();
    } else if (menuType == MenuType.LUNCH) {
        if (restaurant == Restaurant.제2학생회관 || restaurant == Restaurant.제3학생회관) {
            return List.of(Dept.STUDENT, Dept.STAFF);
        } else if (restaurant == Restaurant.상록회관 || restaurant == Restaurant.생활과학대) {
            return List.of(Dept.STUDENT);
        }
    } else if (menuType == MenuType.DINNER) {
        return restaurant == Restaurant.제3학생회관
            ? List.of(Dept.STAFF)
            : Collections.emptyList();
    }
    return Collections.emptyList();
}
```

#### 9. **다중 쿼리: ReviewController.getAllReviews()**
- **위치**: `ReviewController.java:64-74`
- **문제**:
```java
@GetMapping("/total-review")
public List<ReviewResponseDto> getAllReviews(@AuthenticationPrincipal String email) {
    String date = MenuController.getTodayDate();
    List<Review> allReviews = new ArrayList<>();

    for (Restaurant restaurant : Restaurant.values()) {  // ❌ 5번 순회
        List<Review> restaurantReviews = reviewService.findRestaurantReviews(
            restaurant.toString(), date);  // ❌ 각 레스토랑마다 쿼리
        allReviews.addAll(restaurantReviews);
    }

    return getReviewDtos(allReviews, email);
}
```
- **성능 영향**:
  - **5번의 DB 쿼리** + 각각의 메뉴 조회 쿼리
- **해결방안**:
```java
// ReviewService에 메서드 추가
public List<Review> findAllReviewsByDate(String date) {
    // 1학생회관은 날짜 필터링 없이 전체 조회
    List<Review> restaurant1Reviews = reviewRepository.findByRestaurant(Restaurant.제1학생회관);

    // 나머지 레스토랑은 메인 요리 기반 조회
    List<Menu> todayMenus = menuRepository.findByDateAndRestaurantNot(date, Restaurant.제1학생회관);

    // 메인 요리 수집
    Set<Dish> mainDishes = todayMenus.stream()
        .flatMap(menu -> menu.getMainDish().stream())
        .collect(Collectors.toSet());

    // 메인 요리로 관련 메뉴 조회
    List<Menu> relatedMenus = menuRepository.findByMainDishIn(mainDishes);

    // 리뷰 조회
    List<Review> otherReviews = reviewRepository.findByMenuIn(relatedMenus);

    restaurant1Reviews.addAll(otherReviews);
    return restaurant1Reviews;
}

// Controller에서 사용
@GetMapping("/total-review")
public List<ReviewResponseDto> getAllReviews(@AuthenticationPrincipal String email) {
    String date = MenuController.getTodayDate();
    List<Review> allReviews = reviewService.findAllReviewsByDate(date);  // ✅ 한 번의 호출
    return getReviewDtos(allReviews, email);
}
```

#### 10. **비효율적인 알고리즘: MenuService.sortMainDish()**
- **위치**: `MenuService.java:66-83`
- **문제**:
```java
private List<Menu> sortMainDish(List<Menu> menus) {
    List<Menu> sortMenus = new ArrayList<>();

    for (Menu menu : menus) {
        List<Dish> dishList = new ArrayList<>();
        for (Dish dish : menu.getDishList()) {  // ❌ O(n²) 복잡도
            if(dishList.contains(dish))  // ❌ List.contains()는 O(n)
                continue;
            if(dish.getDishType().equals(DishType.MAIN))
                dishList.add(0, dish);  // ❌ List.add(0, ...)는 O(n)
            else
                dishList.add(dish);
        }
        menu.setDishList(dishList);
        sortMenus.add(menu);
    }
    return sortMenus;
}
```
- **시간 복잡도**: O(n³) - 매우 비효율적
- **해결방안**:
```java
private List<Menu> sortMainDish(List<Menu> menus) {
    for (Menu menu : menus) {
        List<Dish> dishes = menu.getDishList();

        // ✅ Set으로 중복 제거 O(n)
        Set<Dish> uniqueDishes = new LinkedHashSet<>(dishes);

        // ✅ Stream API로 정렬 O(n log n)
        List<Dish> sortedDishes = uniqueDishes.stream()
            .sorted((d1, d2) -> {
                // MAIN이 먼저 오도록
                if (d1.getDishType() == DishType.MAIN && d2.getDishType() != DishType.MAIN) {
                    return -1;
                } else if (d1.getDishType() != DishType.MAIN && d2.getDishType() == DishType.MAIN) {
                    return 1;
                }
                return 0;
            })
            .collect(Collectors.toList());

        menu.setDishList(sortedDishes);
    }
    return menus;
}

// 또는 더 깔끔하게
private List<Menu> sortMainDish(List<Menu> menus) {
    for (Menu menu : menus) {
        List<Dish> dishes = new ArrayList<>(new LinkedHashSet<>(menu.getDishList()));

        List<Dish> mainDishes = dishes.stream()
            .filter(d -> d.getDishType() == DishType.MAIN)
            .collect(Collectors.toList());

        List<Dish> sideDishes = dishes.stream()
            .filter(d -> d.getDishType() == DishType.SIDE)
            .collect(Collectors.toList());

        mainDishes.addAll(sideDishes);
        menu.setDishList(mainDishes);
    }
    return menus;
}
```

#### 11. **중복 파일 I/O: RateService**
- **위치**: `RateService.java:36-96`
- **문제**:
```java
@Transactional
public void saveRate(){
    // ❌ JSON 파일 읽기 #1
    String jsonContent = new String(Files.readAllBytes(
        Paths.get("src/main/java/com/SeeAndYouGo/SeeAndYouGo/restaurant/menuOfRestaurant1.json")
            .toAbsolutePath()));
    // ... 파싱 로직
}

public void setRestaurant1MenuField() {
    // ❌ JSON 파일 읽기 #2 (동일한 파일)
    String jsonContent = new String(Files.readAllBytes(
        Paths.get("src/main/java/com/SeeAndYouGo/SeeAndYouGo/restaurant/menuOfRestaurant1.json")
            .toAbsolutePath()));
    // ... 파싱 로직
}
```
- **문제점**:
  - 동일한 파일을 두 번 읽음
  - 파싱 로직 중복
  - Deprecated된 `JsonParser` 사용
- **해결방안**:
```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RateService {

    private Map<String, List<String>> restaurant1MenuByCategory = new HashMap<>();
    private Map<String, Integer> restaurant1MenuByPrice = new HashMap<>();
    private final RateRepository rateRepository;
    private final DishRepository dishRepository;

    // ✅ JSON 데이터를 캐싱
    private Restaurant1MenuData menuData;

    @PostConstruct
    public void init() {
        this.menuData = loadRestaurant1MenuData();
        this.restaurant1MenuByCategory = menuData.getMenuByCategory();
        this.restaurant1MenuByPrice = menuData.getMenuByPrice();
    }

    private Restaurant1MenuData loadRestaurant1MenuData() {
        try {
            String jsonContent = new String(Files.readAllBytes(
                Paths.get("src/main/java/com/SeeAndYouGo/SeeAndYouGo/restaurant/menuOfRestaurant1.json")
                    .toAbsolutePath()
            ));

            // ✅ Gson 권장 방식
            Gson gson = new Gson();
            Restaurant1MenuJson jsonData = gson.fromJson(jsonContent, Restaurant1MenuJson.class);

            Map<String, List<String>> menuByCategory = new HashMap<>();
            Map<String, Integer> menuByPrice = new HashMap<>();

            for (MenuInfo menuInfo : jsonData.getMenuName()) {
                String name = menuInfo.getName();
                String dept = menuInfo.getDept();
                Integer price = menuInfo.getPrice();

                menuByCategory.computeIfAbsent(dept, k -> new ArrayList<>()).add(name);
                menuByPrice.put(name, price);
            }

            return new Restaurant1MenuData(menuByCategory, menuByPrice, jsonData.getMenuName());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load restaurant1 menu data", e);
        }
    }

    @Transactional
    public void saveRate() {
        for (MenuInfo menuInfo : menuData.getMenuItems()) {
            if (!rateRepository.existsByDept(menuInfo.getName())) {
                Rate rate = Rate.builder()
                    .restaurant(Restaurant.제1학생회관)
                    .dept(menuInfo.getName())
                    .build();
                rateRepository.save(rate);
            }
        }
    }

    // DTO 클래스
    @Data
    private static class Restaurant1MenuJson {
        private List<MenuInfo> menuName;
    }

    @Data
    private static class MenuInfo {
        private String name;
        private String dept;
        private Integer price;
    }

    @Data
    @AllArgsConstructor
    private static class Restaurant1MenuData {
        private Map<String, List<String>> menuByCategory;
        private Map<String, Integer> menuByPrice;
        private List<MenuInfo> menuItems;
    }
}
```

#### 13. **매번 객체 생성: DateTimeFormatter**
- **위치**: 여러 곳에서 발견
  - `MenuController.java:82` - getTodayDate()
  - `MenuService.java:90` - getOneWeekRestaurantMenu()
  - `ReviewService.java:104` - sortReviewsByDate()
- **문제**:
```java
// MenuController.java:80-84
public static String getTodayDate() {
    LocalDate currentDate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");  // ❌ 매번 생성
    return currentDate.format(formatter);
}

// ReviewService.java:104
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  // ❌ 매번 생성
```
- **문제점**:
  - `DateTimeFormatter` 생성은 비용이 큼
  - 매번 새로 생성하면 GC 압력 증가
- **해결방안**:
```java
// 공통 유틸리티 클래스 생성
public final class DateTimeUtils {
    // ✅ 상수로 선언하여 재사용
    public static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter DATETIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeUtils() {
        throw new AssertionError("Utility class");
    }

    public static String getTodayDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static LocalDateTime parseDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime, DATETIME_FORMATTER);
    }
}

// 사용
String today = DateTimeUtils.getTodayDate();
LocalDateTime time = DateTimeUtils.parseDateTime(review.getMadeTime());
```

#### 14. **getDishList() 매번 호출 비용**
- **위치**: `Menu.java:119-125`
- **문제**:
```java
public List<Dish> getDishList() {
    List<Dish> dishes = new ArrayList<>();  // ❌ 매번 새로운 리스트 생성
    for(MenuDish menuDish : this.menuDishes){
        dishes.add(menuDish.getDish());
    }
    return dishes;
}

// 이 메서드가 여러 곳에서 호출됨
public List<Dish> getMainDish() {
    List<Dish> dishes = getDishList();  // ❌ 매번 새로운 리스트 생성
    return dishes.stream()
        .filter(dish -> dish.getDishType().equals(DishType.MAIN))
        .collect(Collectors.toList());
}

public List<Dish> getSideDish() {
    List<Dish> dishes = getDishList();  // ❌ 매번 새로운 리스트 생성
    // ...
}
```
- **문제점**:
  - `menuDishes`에서 매번 변환
  - 불필요한 객체 생성
- **해결방안**:
```java
@Entity
@Getter
public class Menu {
    // ... 기존 필드들

    // ✅ Stream으로 직접 접근
    public Stream<Dish> dishStream() {
        return menuDishes.stream().map(MenuDish::getDish);
    }

    public List<Dish> getDishList() {
        return dishStream().collect(Collectors.toList());
    }

    public List<Dish> getMainDish() {
        return dishStream()
            .filter(dish -> dish.getDishType() == DishType.MAIN)
            .collect(Collectors.toList());
    }

    public List<String> getMainDishToString() {
        return dishStream()
            .filter(dish -> dish.getDishType() == DishType.MAIN)
            .map(Dish::toString)
            .collect(Collectors.toList());
    }

    public List<Dish> getSideDish() {
        return dishStream()
            .filter(dish -> dish.getDishType() == DishType.SIDE)
            .collect(Collectors.toList());
    }

    public List<String> getSideDishToString() {
        return dishStream()
            .filter(dish -> dish.getDishType() == DishType.SIDE)
            .map(Dish::toString)
            .collect(Collectors.toList());
    }
}
```

#### 15. **불필요한 Repository 호출: UserService**
- **위치**: `UserService.java` 전체
- **문제**:
```java
public String getNicknameByEmail(String email) {
    User user = userRepository.findByEmail(email);  // ❌ DB 조회
    return user == null ? "익명" : user.getNickname();
}

public String findNickname(String email) {
    User user = userRepository.findByEmail(email);  // ❌ 중복 조회
    return user.getNickname() == null ? "익명" : user.getNickname();
}

public boolean canUpdateNickname(String email) {
    User user = userRepository.findByEmail(email);  // ❌ 중복 조회
    return user.canUpdateNickname(LocalDateTime.now());
}

public String getLastUpdateTimeForNickname(String email) {
    User user = userRepository.findByEmail(email);  // ❌ 중복 조회
    return user.getLastUpdateTime().toLocalDate().toString();
}
```
- **문제점**:
  - 동일한 사용자 정보를 여러 번 조회
  - 메서드 간 중복 로직
  - `getNicknameByEmail`과 `findNickname`이 거의 동일
- **해결방안**:
```java
@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // ✅ 공통 메서드
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean isNicknameCountZero(String nickname) {
        return userRepository.countByNickname(nickname) == 0;
    }

    @Transactional
    public void updateNickname(String email, String nickname) {
        User user = getUserByEmail(email);
        if (user != null) {
            user.changeNickname(nickname);
        }
    }

    // ✅ 메서드 통합
    public String getNickname(String email) {
        User user = getUserByEmail(email);
        if (user == null || user.getNickname() == null) {
            return "익명";
        }
        return user.getNickname();
    }

    public boolean canUpdateNickname(String email) {
        User user = getUserByEmail(email);
        return user != null && user.canUpdateNickname(LocalDateTime.now());
    }

    public String getLastUpdateTimeForNickname(String email) {
        User user = getUserByEmail(email);
        if (user == null || user.getLastUpdateTime() == null) {
            return null;
        }
        return user.getLastUpdateTime().toLocalDate().toString();
    }

    // ✅ 여러 정보가 필요할 때를 위한 DTO 반환
    public UserNicknameInfo getUserNicknameInfo(String email) {
        User user = getUserByEmail(email);
        if (user == null) {
            return UserNicknameInfo.anonymous();
        }

        return UserNicknameInfo.builder()
            .nickname(user.getNickname() != null ? user.getNickname() : "익명")
            .canUpdate(user.canUpdateNickname(LocalDateTime.now()))
            .lastUpdateTime(user.getLastUpdateTime() != null
                ? user.getLastUpdateTime().toLocalDate().toString()
                : null)
            .build();
    }
}

@Data
@Builder
public class UserNicknameInfo {
    private String nickname;
    private boolean canUpdate;
    private String lastUpdateTime;

    public static UserNicknameInfo anonymous() {
        return UserNicknameInfo.builder()
            .nickname("익명")
            .canUpdate(false)
            .lastUpdateTime(null)
            .build();
    }
}
```

---

### 🟡 Medium (중간) - 코드 품질 및 유지보수성

#### 16. **관심사 분리 위반: ReviewController의 이미지 처리**
- **위치**: `ReviewController.java:102-162`
- **문제**:
  - Controller에 이미지 저장 로직이 있음
  - Service에는 resize 로직만 있음
  - 비즈니스 로직과 표현 계층이 혼재
- **해결방안**:
```java
// ImageService 생성
@Service
@RequiredArgsConstructor
public class ImageService {
    private static final String IMAGE_DIR = "imageStorage";
    private static final String TEMP_DIR = "./tmpImage";

    @Qualifier("asyncTaskExecutor")
    private final Executor executor;

    public String saveReviewImage(MultipartFile image) {
        if (image == null) {
            return "";
        }

        String imgName = generateImageName();
        File tempFile = createTempFile(image);
        saveImageAsync(tempFile, imgName);

        return "/api/images/" + imgName;
    }

    private String generateImageName() {
        return UUID.randomUUID() +
               LocalDateTime.now().toString().replace(".", "").replace(":", "") +
               ".png";
    }

    private File createTempFile(MultipartFile image) {
        File dir = new File(TEMP_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(String.format("%s/%s.png", dir.getPath(), UUID.randomUUID()));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file", e);
        }

        return file;
    }

    private void saveImageAsync(File tempFile, String imgName) {
        executor.execute(() -> {
            try {
                Files.createDirectories(Paths.get(IMAGE_DIR));
                Path targetPath = Paths.get(IMAGE_DIR, imgName);
                BufferedImage resized = resize(tempFile);
                ImageIO.write(resized, "png", new File(targetPath.toUri()));
                tempFile.delete();
            } catch (Exception e) {
                log.error("[리뷰업로드] 오류 {}", e.getMessage());
            }
        });
    }

    public BufferedImage resize(File file) throws Exception {
        // 기존 ReviewService의 resize 로직
    }

    public byte[] getImage(String imgName) throws Exception {
        // 파일명 검증 (보안)
        if (imgName.contains("..") || imgName.contains("/") || imgName.contains("\\")) {
            throw new IllegalArgumentException("Invalid image name");
        }

        if (!imgName.matches("^[a-f0-9-]+\\.png$")) {
            throw new IllegalArgumentException("Invalid image format");
        }

        Path imagePath = Paths.get(IMAGE_DIR, imgName).normalize();

        if (!imagePath.startsWith(Paths.get(IMAGE_DIR).toAbsolutePath())) {
            throw new SecurityException("Access denied");
        }

        if (!Files.exists(imagePath)) {
            throw new FileNotFoundException("Image not found");
        }

        return Files.readAllBytes(imagePath);
    }

    public void deleteImage(String imgLink) {
        if (imgLink == null || imgLink.isEmpty()) {
            return;
        }

        // "/api/images/"를 제거하고 파일명만 추출
        String imgName = imgLink.replace("/api/images/", "");
        File imageFile = new File(IMAGE_DIR + File.separator + imgName);

        if (imageFile.exists()) {
            if (imageFile.delete()) {
                log.info("Deleted image file: {}", imageFile.getAbsolutePath());
            } else {
                log.error("Failed to delete image file: {}", imageFile.getAbsolutePath());
            }
        } else {
            log.warn("Image file not found: {}", imageFile.getAbsolutePath());
        }
    }
}

// ReviewController에서 사용
@PostMapping(value = "/review")
@ResponseStatus(HttpStatus.CREATED)
public Long postReview(@RequestPart(value = "dto") ReviewRequestDto dto,
                       @RequestPart(value = "image", required = false) MultipartFile image,
                       @AuthenticationPrincipal String email) {
    String nickname = userService.findNickname(email);
    String imgUrl = imageService.saveReviewImage(image);  // ✅ 깔끔!

    ReviewData data = ReviewData.builder()
        .restaurant(Restaurant.parseName(dto.getRestaurant()))
        .menuId(dto.getMenuId())
        .dept(dto.getDept())
        .menuName(dto.getMenuName())
        .rate(dto.getRate())
        .email(email)
        .nickName(dto.isAnonymous() ? "익명" : nickname)
        .comment(dto.getComment())
        .imgUrl(imgUrl)
        .build();

    return reviewService.registerReview(data);
}

@GetMapping("/images/{imgName}")
public byte[] showImage(@PathVariable String imgName) throws Exception {
    return imageService.getImage(imgName);  // ✅ 보안 검증 포함
}

// ReviewService.deleteById에서 사용
@Transactional
public void deleteById(Long reviewId) {
    Review review = reviewRepository.getReferenceById(reviewId);
    reviewRepository.deleteById(reviewId);

    imageService.deleteImage(review.getImgLink());  // ✅ 이미지 삭제 위임

    review.getMenu().deleteReview(review);

    ReviewHistory reviewHistory = new ReviewHistory(review);
    reviewHistoryRepository.save(reviewHistory);
}
```

#### 17. **복잡한 조건문: MenuService.fillMenu()**
- **위치**: `MenuService.java:133-165`
- **문제**:
```java
private void fillMenu(Restaurant restaurant, List<Menu> menus, String date) {
    for (MenuType menuType : MenuType.values()) {
        if(menuType.equals(MenuType.BREAKFAST)){
            if(restaurant.equals(Restaurant.제2학생회관)){
                checkMenuByDeptAndMenuType(restaurant, menus, date, Dept.STUDENT, MenuType.BREAKFAST);
            }
        }else if(menuType.equals(MenuType.LUNCH)){
            if(restaurant.equals(Restaurant.제2학생회관) || restaurant.equals(Restaurant.제3학생회관)){
                checkMenuByDeptAndMenuType(restaurant, menus, date, Dept.STUDENT, MenuType.LUNCH);
                checkMenuByDeptAndMenuType(restaurant, menus, date, Dept.STAFF, MenuType.LUNCH);
            }else if(restaurant.equals(Restaurant.상록회관) || restaurant.equals(Restaurant.생활과학대)){
                checkMenuByDeptAndMenuType(restaurant, menus, date, Dept.STUDENT, MenuType.LUNCH);
            }
        }else{
            if(restaurant.equals(Restaurant.제3학생회관)){
                checkMenuByDeptAndMenuType(restaurant, menus, date, Dept.STAFF, MenuType.DINNER);
            }
        }
    }
}
```
- **문제점**:
  - 중첩된 조건문
  - 하드코딩된 비즈니스 규칙
  - 확장성 부족
- **해결방안**:
```java
// 전략 패턴 적용
@Component
public class MenuValidationStrategy {

    private static final Map<MenuType, Map<Restaurant, List<Dept>>> MENU_RULES = new HashMap<>();

    static {
        // BREAKFAST 규칙
        Map<Restaurant, List<Dept>> breakfastRules = new HashMap<>();
        breakfastRules.put(Restaurant.제2학생회관, List.of(Dept.STUDENT));
        MENU_RULES.put(MenuType.BREAKFAST, breakfastRules);

        // LUNCH 규칙
        Map<Restaurant, List<Dept>> lunchRules = new HashMap<>();
        lunchRules.put(Restaurant.제2학생회관, List.of(Dept.STUDENT, Dept.STAFF));
        lunchRules.put(Restaurant.제3학생회관, List.of(Dept.STUDENT, Dept.STAFF));
        lunchRules.put(Restaurant.상록회관, List.of(Dept.STUDENT));
        lunchRules.put(Restaurant.생활과학대, List.of(Dept.STUDENT));
        MENU_RULES.put(MenuType.LUNCH, lunchRules);

        // DINNER 규칙
        Map<Restaurant, List<Dept>> dinnerRules = new HashMap<>();
        dinnerRules.put(Restaurant.제3학생회관, List.of(Dept.STAFF));
        MENU_RULES.put(MenuType.DINNER, dinnerRules);
    }

    public List<Dept> getRequiredDepts(Restaurant restaurant, MenuType menuType) {
        return MENU_RULES.getOrDefault(menuType, Collections.emptyMap())
                         .getOrDefault(restaurant, Collections.emptyList());
    }
}

// MenuService에서 사용
@RequiredArgsConstructor
public class MenuService {
    private final MenuValidationStrategy menuValidationStrategy;

    private void fillMenu(Restaurant restaurant, List<Menu> menus, String date) {
        for (MenuType menuType : MenuType.values()) {
            List<Dept> requiredDepts = menuValidationStrategy.getRequiredDepts(restaurant, menuType);

            for (Dept dept : requiredDepts) {
                checkMenuByDeptAndMenuType(restaurant, menus, date, dept, menuType);
            }
        }
    }
}
```

#### 18. **코드 중복: DTO 변환 로직**
- **위치**: `MenuController.java`
- **문제**:
```java
// line 86-93
private List<MenuResponseDto> parseOneDayRestaurantMenu(List<Menu> oneDayRestaurantMenu) {
    List<MenuResponseDto> menuResponseDtos = new ArrayList<>();
    for (Menu dayRestaurantMenu : oneDayRestaurantMenu) {
        MenuResponseDto menuResponseDto = new MenuResponseDto(dayRestaurantMenu);
        menuResponseDtos.add(menuResponseDto);
    }
    return menuResponseDtos;
}

// line 95-102 - 거의 동일한 로직
private List<MenuResponseByAdminDto> parseOneDayRestaurantMenuForAdmin(List<Menu> oneDayRestaurantMenu) {
    List<MenuResponseByAdminDto> menuResponseDtos = new ArrayList<>();
    for (Menu dayRestaurantMenu : oneDayRestaurantMenu) {
        MenuResponseByAdminDto menuResponseDto = new MenuResponseByAdminDto(dayRestaurantMenu);
        menuResponseDtos.add(menuResponseDto);
    }
    return menuResponseDtos;
}

// line 107-131 - 또 다른 변형
private List<MenuResponseByUserDto> parseOneDayRestaurantMenuByUser(...) {
    List<MenuResponseByUserDto> menuResponseDtos = new ArrayList<>();
    for (Menu menu : oneDayRestaurantMenu) {
        MenuResponseByUserDto dto = MenuResponseByUserDto.builder()
            // ... 빌더 패턴
            .build();
        menuResponseDtos.add(dto);
    }
    return menuResponseDtos;
}
```
- **해결방안**:
```java
// 제네릭 메서드로 통합
private <T> List<T> convertMenuList(List<Menu> menus, Function<Menu, T> converter) {
    return menus.stream()
        .map(converter)
        .collect(Collectors.toList());
}

// 사용
private List<MenuResponseDto> parseOneDayRestaurantMenu(List<Menu> menus) {
    return convertMenuList(menus, MenuResponseDto::new);
}

private List<MenuResponseByAdminDto> parseOneDayRestaurantMenuForAdmin(List<Menu> menus) {
    return convertMenuList(menus, MenuResponseByAdminDto::new);
}

private List<MenuResponseByUserDto> parseOneDayRestaurantMenuByUser(
        List<Menu> menus, List<String> keywords, List<String> newMainDishs) {
    return menus.stream()
        .map(menu -> MenuResponseByUserDto.builder()
            .menuId(menu.getId())
            .menuType(menu.getMenuType().toString())
            .price(menu.getPrice())
            .dept(menu.getDept().toString())
            .sideDishList(menu.getSideDishToString())
            .mainDishList(menu.getMainDishToString())
            .restaurantName(menu.getRestaurant().toString())
            .date(menu.getDate())
            .keywordList(keywords)
            .isOpen(menu.isOpen())
            .newDishList(newMainDishs)
            .build())
        .collect(Collectors.toList());
}
```

#### 19. **예외 처리 미흡**
- **위치**:
  - `DishService.java:89-98` - deleteDish()
  - `ReviewService.java:209-218` - deleteReportedReview()
- **문제**:
```java
// DishService
@Transactional
public boolean deleteDish(Long id) {
    try{
        menuDishRepository.deleteByDishId(id);
        dishRepository.deleteById(id);
    }catch (Exception e){  // ❌ 모든 예외를 무시
        return false;
    }
    return true;
}

// ReviewService
@Transactional
public boolean deleteReportedReview(Long reviewId) {
    try{
        reviewRepository.deleteById(reviewId);
    }catch (Exception e){  // ❌ 예외 무시
        return false;
    }
    return true;
}
```
- **문제점**:
  - 예외 정보 손실
  - 디버깅 불가능
  - 실패 원인 파악 불가
- **해결방안**:
```java
// 커스텀 예외 정의
public class DishDeletionException extends RuntimeException {
    public DishDeletionException(String message, Throwable cause) {
        super(message, cause);
    }
}

// DishService
@Transactional
public void deleteDish(Long id) {
    try {
        menuDishRepository.deleteByDishId(id);
        dishRepository.deleteById(id);
    } catch (Exception e) {
        log.error("Failed to delete dish with id: {}", id, e);
        throw new DishDeletionException("요리 삭제에 실패했습니다. ID: " + id, e);
    }
}

// ReviewService
@Transactional
public void deleteReportedReview(Long reviewId) {
    try {
        reviewRepository.deleteById(reviewId);
    } catch (Exception e) {
        log.error("Failed to delete reported review with id: {}", reviewId, e);
        throw new ReviewDeletionException("신고된 리뷰 삭제에 실패했습니다. ID: " + reviewId, e);
    }
}

// Controller에서 처리
@DeleteMapping("/review/report/{reviewId}")
public ReviewDeleteResponseDto deleteReportedReview(@PathVariable Long reviewId) {
    try {
        reviewService.deleteReportedReview(reviewId);
        return new ReviewDeleteResponseDto(true);
    } catch (ReviewDeletionException e) {
        log.warn("Review deletion failed", e);
        return new ReviewDeleteResponseDto(false);
    }
}
```

#### 20. **static 메서드 남용: MenuController.getTodayDate()**
- **위치**: `MenuController.java:80-84`
- **문제**:
```java
public static String getTodayDate() {
    LocalDate currentDate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    return currentDate.format(formatter);
}
```
- **문제점**:
  - 테스트하기 어려움 (시간을 모킹할 수 없음)
  - Controller에서 다른 Controller의 static 메서드 호출
  - 의존성 관리 불명확
- **해결방안**:
```java
// DateTimeService 생성
@Service
public class DateTimeService {
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String getTodayDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public LocalDate getToday() {
        return LocalDate.now();
    }

    // 테스트를 위한 메서드 (필요시)
    public String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
}

// Controller에서 사용
@RestController
@RequiredArgsConstructor
public class MenuController {
    private final MenuService menuService;
    private final DateTimeService dateTimeService;

    @GetMapping("/daily-menu/{restaurant}")
    public List<MenuResponseByUserDto> restaurantMenuDayByUser(
            @PathVariable("restaurant") String place,
            @AuthenticationPrincipal String email) {
        String today = dateTimeService.getTodayDate();  // ✅ 주입 받아 사용
        return restaurantMenuDayByUserTest(place, today, email);
    }
}

// 테스트 가능
@Test
void testGetTodayMenu() {
    DateTimeService dateTimeService = mock(DateTimeService.class);
    when(dateTimeService.getTodayDate()).thenReturn("2025-11-01");

    // 테스트 로직
}
```

#### 21. **중복 코드: Restaurant.parseName() 호출**
- **위치**: 여러 Service와 Controller
- **문제**:
```java
// MenuService.java:54
String parseRestaurantName = Restaurant.parseName(restaurantName);
Restaurant restaurant = Restaurant.valueOf(parseRestaurantName);

// ReviewService.java:126
Restaurant restaurant = Restaurant.valueOf(Restaurant.parseName(restaurantName));

// ConnectionService.java:33
String parseRestaurantName = Restaurant.parseName(restaurantName);
Restaurant restaurant = Restaurant.valueOf(parseRestaurantName);
```
- **문제점**:
  - 동일한 패턴이 20곳 이상 반복
  - 중복 코드
- **해결방안**:
```java
// Restaurant enum에 추가
public enum Restaurant {
    제1학생회관("1학생회관", ...),
    제2학생회관("2학생회관", ...),
    // ...

    public static Restaurant parse(String name) {
        String parsedName = parseName(name);
        return valueOf(parsedName);
    }

    // 또는 Optional 반환
    public static Optional<Restaurant> parseOptional(String name) {
        try {
            return Optional.of(parse(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}

// 사용
Restaurant restaurant = Restaurant.parse(restaurantName);

// 또는 안전하게
Restaurant restaurant = Restaurant.parseOptional(restaurantName)
    .orElseThrow(() -> new IllegalArgumentException("Invalid restaurant: " + restaurantName));
```

#### 22. **Transactional 범위 불명확**
- **위치**: `UserService.java:10`
- **문제**:
```java
@Service
@Transactional(readOnly = false)  // ❌ 클래스 레벨에 readOnly = false
@RequiredArgsConstructor
public class UserService {
    // 대부분의 메서드가 읽기 전용인데 false로 설정
    public String getNickname(String email) { ... }
    public boolean canUpdateNickname(String email) { ... }

    // 쓰기가 필요한 메서드만 일부
    @Transactional
    public void updateNickname(String email, String nickname) { ... }
}
```
- **문제점**:
  - 불필요한 트랜잭션 오버헤드
  - 읽기 전용인데 쓰기 트랜잭션 사용
- **해결방안**:
```java
@Service
@Transactional(readOnly = true)  // ✅ 기본은 읽기 전용
@RequiredArgsConstructor
public class UserService {
    // 읽기 메서드들 (기본 readOnly = true 적용)
    public String getNickname(String email) { ... }
    public boolean canUpdateNickname(String email) { ... }

    // 쓰기가 필요한 메서드만 명시적으로 readOnly = false
    @Transactional(readOnly = false)
    public void updateNickname(String email, String nickname) {
        User user = userRepository.findByEmail(email);
        user.changeNickname(nickname);
    }
}
```

#### 23. **하드코딩된 값**
- **위치**: 여러 곳
- **문제**:
```java
// ReviewController.java:36
@CrossOrigin(origins = "http://localhost:3000")  // ❌ 하드코딩

// ReviewController.java:102
private static final String IMAGE_DIR = "imageStorage";  // ❌ 하드코딩

// ReviewService.java:36
private static final int TOP_REVIEW_NUMBER_OF_CRITERIA = 3;  // ❌ 설정으로 분리 필요
```
- **해결방안**:
```yaml
# application.yml
app:
  cors:
    allowed-origins: http://localhost:3000,https://yourdomain.com
  image:
    storage-dir: ${IMAGE_STORAGE_DIR:imageStorage}
    temp-dir: ${IMAGE_TEMP_DIR:./tmpImage}
  review:
    top-review-count: 3
```

```java
@Configuration
public class AppConfig {
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${app.image.storage-dir}")
    private String imageStorageDir;

    @Value("${app.review.top-review-count}")
    private int topReviewCount;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins(allowedOrigins)
                    .allowedMethods("*");
            }
        };
    }
}

// Controller에서 @CrossOrigin 제거
@RestController
@RequestMapping("/api")
public class ReviewController {
    // @CrossOrigin 제거
}

// Service에서 사용
@Service
@RequiredArgsConstructor
public class ReviewService {
    @Value("${app.review.top-review-count}")
    private int topReviewCount;

    // private static final int TOP_REVIEW_NUMBER_OF_CRITERIA = 3; 제거
}
```

#### 24. **Repository 메서드 이름 개선**
- **위치**: Repository 인터페이스들
- **문제**:
```java
// RateRepository
Rate findByRestaurantAndDept(Restaurant restaurant, String dept);  // ❌ dept는 String?

// ReviewRepository
List<Review> findByRestaurantAndMenuIn(Restaurant restaurant, List<Menu> date);  // ❌ 파라미터명 잘못됨
```
- **해결방안**:
```java
// RateRepository
Rate findByRestaurantAndDept(Restaurant restaurant, String deptName);

// 또는 Dept enum 사용
@Query("SELECT r FROM Rate r WHERE r.restaurant = :restaurant AND r.dept = :dept")
Rate findByRestaurantAndDept(@Param("restaurant") Restaurant restaurant,
                             @Param("dept") String dept);

// ReviewRepository - 명확한 이름
List<Review> findByRestaurantAndMenuIn(Restaurant restaurant, List<Menu> menus);
```

#### 25. **DTO 중복**
- **위치**: `MenuResponseDto`, `MenuResponseByAdminDto`, `MenuResponseByUserDto`
- **문제**:
  - 거의 동일한 필드를 가진 3개의 DTO
  - 유지보수성 저하
- **해결방안**:
```java
// 기본 DTO
@Data
@Builder
public class MenuResponseDto {
    private Long menuId;
    private String menuType;
    private Integer price;
    private String dept;
    private List<String> sideDishList;
    private List<String> mainDishList;
    private String restaurantName;
    private String date;
    private boolean isOpen;

    // User 전용 필드 (Optional)
    private List<String> keywordList;
    private List<String> newDishList;

    public static MenuResponseDto fromMenu(Menu menu) {
        return MenuResponseDto.builder()
            .menuId(menu.getId())
            .menuType(menu.getMenuType().toString())
            .price(menu.getPrice())
            .dept(menu.getDept().toString())
            .sideDishList(menu.getSideDishToString())
            .mainDishList(menu.getMainDishToString())
            .restaurantName(menu.getRestaurant().toString())
            .date(menu.getDate())
            .isOpen(menu.isOpen())
            .build();
    }

    public static MenuResponseDto fromMenuForUser(Menu menu, List<String> keywords, List<String> newDishes) {
        MenuResponseDto dto = fromMenu(menu);
        dto.setKeywordList(keywords);
        dto.setNewDishList(newDishes);
        return dto;
    }
}
```

#### 26. **매직 넘버**
- **위치**: 여러 곳
- **문제**:
```java
// Menu.java:127-131
this.rate = (this.rate + review.getReviewRate()) / this.reviewList.size();  // 평균 계산

// MenuService.java:95
List<Menu>[] weekMenuList = new List[7];  // ❌ 7은 무엇?

// ReviewService.java:36
private static final int TOP_REVIEW_NUMBER_OF_CRITERIA = 3;
```
- **해결방안**:
```java
// 상수로 정의
public final class Constants {
    public static final int DAYS_IN_WEEK = 7;
    public static final int DEFAULT_TOP_REVIEW_COUNT = 3;
    public static final int NICKNAME_UPDATE_COOLDOWN_DAYS = 14;

    private Constants() {
        throw new AssertionError("Utility class");
    }
}

// 사용
List<Menu>[] weekMenuList = new List[Constants.DAYS_IN_WEEK];
```

#### 27. **Optional 미사용**
- **위치**: 여러 Service
- **문제**:
```java
// UserService.java:25-28
public String getNicknameByEmail(String email) {
    User user = userRepository.findByEmail(email);
    return user == null ? "익명" : user.getNickname();  // ❌ null 체크
}

// ReviewService.java:146
Review review = reviewRepository.findById(reviewId).get();  // ❌ .get() 직접 호출
```
- **해결방안**:
```java
// UserRepository
Optional<User> findByEmail(String email);

// UserService
public String getNicknameByEmail(String email) {
    return userRepository.findByEmail(email)
        .map(User::getNickname)
        .orElse("익명");
}

// ReviewService
Review review = reviewRepository.findById(reviewId)
    .orElseThrow(() -> new EntityNotFoundException("Review not found: " + reviewId));
```

#### 28. **Enum 파싱 중복**
- **위치**: 여러 Controller
- **문제**:
```java
// MenuController.java:119
.dept(menu.getDept().toString())

// 여러 곳에서 Dept.valueOf() 또는 Restaurant.valueOf() 호출
```
- **해결방안**:
```java
// Spring Converter 사용
@Component
public class StringToRestaurantConverter implements Converter<String, Restaurant> {
    @Override
    public Restaurant convert(String source) {
        return Restaurant.parse(source);
    }
}

@Component
public class StringToDeptConverter implements Converter<String, Dept> {
    @Override
    public Dept convert(String source) {
        return Dept.valueOf(source.toUpperCase());
    }
}

// WebMvcConfigurer에 등록
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToRestaurantConverter());
        registry.addConverter(new StringToDeptConverter());
    }
}

// Controller에서 자동 변환
@GetMapping("/daily-menu/{restaurant}")
public List<MenuResponseByUserDto> restaurantMenuDayByUser(
        @PathVariable Restaurant restaurant,  // ✅ 자동 변환
        @AuthenticationPrincipal String email) {
    // 직접 파싱 불필요
}
```

#### 29. **로깅 개선 필요**
- **위치**: 전체
- **문제**:
  - 일관성 없는 로깅 레벨
  - 중요 정보 누락
  - 과도한 로깅 또는 부족한 로깅
- **해결방안**:
```java
// Service 메서드에 로깅 추가
@Transactional
public void saveWeeklyMenu(Restaurant restaurant, LocalDate monday, LocalDate sunday) throws Exception {
    log.info("Starting weekly menu save for restaurant: {}, period: {} ~ {}",
             restaurant, monday, sunday);

    try {
        MenuProvider menuProvider = menuProviderFactory.createMenuProvider(restaurant);
        menuProvider.updateMenuMap(restaurant, monday, sunday);

        List<MenuVO> weeklyMenu = menuProvider.getWeeklyMenu(restaurant);
        log.debug("Fetched {} menus for restaurant: {}", weeklyMenu.size(), restaurant);

        // ... 저장 로직

        log.info("Successfully saved weekly menu for restaurant: {}", restaurant);
    } catch (Exception e) {
        log.error("Failed to save weekly menu for restaurant: {}", restaurant, e);
        throw e;
    }
}

// AOP로 통합 로깅
@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {
    @Around("execution(* com.SeeAndYouGo.SeeAndYouGo..service.*.*(..))")
    public Object logServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.debug("Executing {}, args: {}", methodName, args);

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            log.debug("Completed {} in {}ms", methodName, executionTime);

            return result;
        } catch (Exception e) {
            log.error("Exception in {}: {}", methodName, e.getMessage());
            throw e;
        }
    }
}
```

#### 30. **테스트 코드 부재**
- **위치**: 전체 프로젝트
- **문제**:
  - 단위 테스트 부족
  - 통합 테스트 부족
  - 리팩토링 시 안정성 보장 어려움
- **해결방안**:
```java
// 예시: MenuService 테스트
@SpringBootTest
@Transactional
class MenuServiceTest {

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private DishRepository dishRepository;

    @Test
    @DisplayName("주간 메뉴 조회 - 7일치 데이터 반환")
    void getOneWeekRestaurantMenu_shouldReturn7Days() {
        // given
        String restaurant = "2학생회관";
        String date = "2025-11-04"; // 월요일

        // when
        List<Menu>[] result = menuService.getOneWeekRestaurantMenu(restaurant, date);

        // then
        assertThat(result).hasSize(7);
        assertThat(result[0].get(0).getDate()).isEqualTo("2025-11-04");
        assertThat(result[6].get(0).getDate()).isEqualTo("2025-11-10");
    }

    @Test
    @DisplayName("메인 요리 정렬 - MAIN 타입이 먼저 오는지 확인")
    void sortMainDish_shouldPutMainDishFirst() {
        // given
        Menu menu = createMenuWithDishes();

        // when
        List<Menu> result = menuService.sortMainDish(List.of(menu));

        // then
        List<Dish> dishes = result.get(0).getDishList();
        assertThat(dishes.get(0).getDishType()).isEqualTo(DishType.MAIN);
    }
}

// Repository 테스트
@DataJpaTest
class MenuRepositoryTest {

    @Autowired
    private MenuRepository menuRepository;

    @Test
    @DisplayName("레스토랑과 날짜 범위로 메뉴 조회")
    void findByRestaurantAndDateBetween() {
        // given
        // 테스트 데이터 생성

        // when
        List<Menu> result = menuRepository.findByRestaurantAndDateBetween(
            Restaurant.제2학생회관,
            "2025-11-01",
            "2025-11-07"
        );

        // then
        assertThat(result).isNotEmpty();
    }
}

// Controller 테스트
@WebMvcTest(MenuController.class)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuService menuService;

    @Test
    @DisplayName("일일 메뉴 조회 API")
    void getDailyMenu() throws Exception {
        // given
        List<Menu> menus = createTestMenus();
        when(menuService.getOneDayRestaurantMenu(any(), any())).thenReturn(menus);

        // when & then
        mockMvc.perform(get("/api/daily-menu/2학생회관"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].restaurantName").value("제2학생회관"));
    }
}
```

---

### 🟢 Low (낮음) - 마이너 개선

#### 31. **Deprecated API 사용**
- **위치**: `RateService.java:43`
- **문제**:
```java
JsonParser jsonParser = new JsonParser();  // ❌ Deprecated in Gson 2.8.9
JsonObject jsonData = jsonParser.parse(jsonContent).getAsJsonObject();
```
- **해결방안**:
```java
Gson gson = new Gson();
JsonObject jsonData = gson.fromJson(jsonContent, JsonObject.class);

// 또는 Jackson 사용
ObjectMapper mapper = new ObjectMapper();
Restaurant1MenuJson data = mapper.readValue(jsonContent, Restaurant1MenuJson.class);
```

#### 32. **불필요한 변수**
- **위치**: 여러 곳
- **문제**:
```java
// ReviewController.java:66
String date = MenuController.getTodayDate();
List<Review> allReviews = new ArrayList<>();  // ❌ 바로 할당하지 않고 선언만

for (Restaurant restaurant : Restaurant.values()) {
    List<Review> restaurantReviews = reviewService.findRestaurantReviews(...);
    allReviews.addAll(restaurantReviews);
}
```
- **해결방안**:
```java
String date = MenuController.getTodayDate();

List<Review> allReviews = Arrays.stream(Restaurant.values())
    .flatMap(restaurant -> reviewService.findRestaurantReviews(
        restaurant.toString(), date).stream())
    .collect(Collectors.toList());
```

#### 33. **메서드 명명 개선**
- **위치**: 여러 Service
- **문제**:
```java
// MenuService.java:199
public Dish getDefaultDish()  // ❌ get인데 없으면 생성?

// UserService.java:15
public boolean isNicknameCountZero(String nickname)  // ❌ 부정형
```
- **해결방안**:
```java
public Dish getOrCreateDefaultDish()  // ✅ 명확한 의도

public boolean isNicknameAvailable(String nickname)  // ✅ 긍정형
```

#### 34. **주석 개선**
- **위치**: 여러 곳
- **문제**:
```java
// MenuService.java:56
List<Menu> menus = menuRepository.findByRestaurantAndDate(restaurant, date);  // 검색을 여기서 하는게 낫지 않나 싶기두 ~

// 불명확하거나 불필요한 주석
```
- **해결방안**:
  - 코드로 설명 (self-documenting code)
  - 필요한 경우 JavaDoc 작성
  - 불필요한 주석 제거

#### 35. **매개변수 검증 부족**
- **위치**: 여러 Service 메서드
- **문제**:
```java
public List<Menu> getOneDayRestaurantMenu(String restaurantName, String date) {
    // ❌ null 체크 없음
    // ❌ 빈 문자열 체크 없음
    String parseRestaurantName = Restaurant.parseName(restaurantName);
    // ...
}
```
- **해결방안**:
```java
public List<Menu> getOneDayRestaurantMenu(String restaurantName, String date) {
    Objects.requireNonNull(restaurantName, "Restaurant name cannot be null");
    Objects.requireNonNull(date, "Date cannot be null");

    if (restaurantName.trim().isEmpty()) {
        throw new IllegalArgumentException("Restaurant name cannot be empty");
    }

    // 또는 Spring Validation 사용
}

// Controller에서
@GetMapping("/daily-menu/{restaurant}/{date}")
public List<MenuResponseByUserDto> restaurantMenuDayByUserTest(
        @PathVariable @NotBlank String restaurant,
        @PathVariable @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}") String date,
        @AuthenticationPrincipal String email) {
    // ...
}
```

#### 36. **Builder 패턴 일관성**
- **위치**: Entity 클래스들
- **문제**:
  - 어떤 곳은 Builder, 어떤 곳은 생성자
  - 일관성 부족
- **해결방안**:
  - 모든 Entity에 Builder 적용
  - 또는 생성자 + 정적 팩토리 메서드 사용

#### 37. **리소스 정리**
- **위치**: `ReviewController.java:140`
- **문제**:
```java
File file = new File(...);
try (FileOutputStream fos = new FileOutputStream(file)) {  // ✅ try-with-resources 사용
    fos.write(image.getBytes());
} catch (IOException e) {
    throw new RuntimeException(e);
}
// ❌ file 삭제 안함 (tempFile이 남음)
```
- **해결방안**:
```java
File tempFile = null;
try {
    tempFile = createTempFileFromMultipart(image);
    saveImageAsync(tempFile, imgName);
} finally {
    // 비동기 저장이 완료되기 전에 삭제되지 않도록 주의
    // 비동기 작업 내에서 삭제하는 것이 더 안전
}
```

#### 38. **Stream API 활용 부족**
- **위치**: 여러 곳
- **문제**:
```java
// ReviewService.java:92-100
public void splitStudentAndStaff(List<Review> restaurantReviews,
                                  List<Review> studentReviews,
                                  List<Review> staffReviews) {
    for (Review review : restaurantReviews) {
        if(review.getMenu().getDept().equals(Dept.STAFF)){
            staffReviews.add(review);
            continue;
        }
        studentReviews.add(review);
    }
}
```
- **해결방안**:
```java
// Map으로 그룹핑
Map<Dept, List<Review>> reviewsByDept = restaurantReviews.stream()
    .collect(Collectors.partitioningBy(
        review -> review.getMenu().getDept() == Dept.STAFF,
        Collectors.mapping(Function.identity(), Collectors.toList())
    ));

List<Review> staffReviews = reviewsByDept.get(true);
List<Review> studentReviews = reviewsByDept.get(false);

// 또는 더 명확하게
Map<Dept, List<Review>> reviewsByDept = restaurantReviews.stream()
    .collect(Collectors.groupingBy(review -> review.getMenu().getDept()));
```

---

## 리팩토링 로드맵

### Phase 1: Critical 수정 (1주차)
**목표**: 보안 취약점과 데이터 정합성 문제 해결

1. ✅ **보안 취약점 수정** (1일)
   - [ ] ReviewController.showImage() 경로 검증 추가
   - [ ] 파일명 화이트리스트 검증
   - [ ] 보안 테스트 작성

2. ✅ **데이터 정합성 수정** (1일)
   - [ ] Menu.addReviewAndUpdateRate() 평균 계산 수정
   - [ ] Menu.deleteReview() 평균 계산 수정
   - [ ] 기존 데이터 마이그레이션 스크립트 작성

3. ✅ **버그 수정** (1일)
   - [ ] ConnectionService.saveRecentConnection() return → continue
   - [ ] 통합 테스트 작성

### Phase 2: High Priority 성능 개선 (2-3주차)
**목표**: N+1 쿼리 제거 및 주요 성능 병목 해결

1. ✅ **N+1 쿼리 제거** (5일)
   - [ ] DishService.updateMainDish() 일괄 조회
   - [ ] MenuService.saveWeeklyMenu() 일괄 조회
   - [ ] ReviewService.findRestaurantReviews() 최적화
   - [ ] Repository 메서드 추가 (findByNameIn, etc.)
   - [ ] 성능 테스트 작성

2. ✅ **다중 쿼리 최적화** (3일)
   - [ ] MenuService.getOneWeekRestaurantMenu() 한 번에 조회
   - [ ] MenuService.checkWeekMenu() 일괄 처리
   - [ ] ReviewController.getAllReviews() 최적화
   - [ ] 벤치마크 테스트

3. ✅ **알고리즘 개선** (2일)
   - [ ] MenuService.sortMainDish() O(n log n)으로 개선
   - [ ] ReviewService.sortReviewsByDate() 최적화
   - [ ] Menu.getDishList() 캐싱 또는 Stream 활용

### Phase 3: Medium Priority 코드 품질 개선 (4-5주차)
**목표**: 설계 개선 및 유지보수성 향상

1. ✅ **관심사 분리** (3일)
   - [ ] ImageService 생성 및 이미지 로직 이관
   - [ ] DateTimeService 생성
   - [ ] Controller 슬림화

2. ✅ **코드 중복 제거** (3일)
   - [ ] DTO 변환 로직 통합
   - [ ] Restaurant.parse() 헬퍼 메서드
   - [ ] DateTimeFormatter 상수화
   - [ ] RateService JSON 로딩 통합

3. ✅ **설계 개선** (4일)
   - [ ] MenuValidationStrategy 도입
   - [ ] 커스텀 예외 정의
   - [ ] Optional 적극 활용
   - [ ] Transactional 범위 최적화

### Phase 4: Low Priority 마이너 개선 (6주차)
**목표**: 코드 품질 향상 및 표준 준수

1. ✅ **코드 정리** (2일)
   - [ ] Deprecated API 제거
   - [ ] 불필요한 변수 제거
   - [ ] 메서드 명명 개선
   - [ ] 주석 정리

2. ✅ **Spring 표준 활용** (2일)
   - [ ] Converter 등록 (Enum 자동 변환)
   - [ ] Validation 적용
   - [ ] Configuration 외부화

3. ✅ **로깅 및 모니터링** (1일)
   - [ ] 일관된 로깅 전략 수립
   - [ ] AOP 로깅 적용
   - [ ] 성능 모니터링 포인트 추가

### Phase 5: 테스트 및 문서화 (지속적)
**목표**: 안정성 확보 및 지식 공유

1. ✅ **테스트 코드 작성**
   - [ ] 단위 테스트 (Service 레이어)
   - [ ] 통합 테스트 (Repository + Service)
   - [ ] API 테스트 (Controller)
   - [ ] 성능 테스트

2. ✅ **문서화**
   - [ ] API 문서 (Swagger/SpringDoc)
   - [ ] 아키텍처 문서
   - [ ] 개발 가이드

---

## 예상 효과

### 성능 개선
- **DB 쿼리 수 감소**: 70% 이상 감소 예상
  - 주간 메뉴 조회: 7회 → 1회
  - 메뉴 체크: 28회 → 1회
  - Dish 업데이트: 100회 → 2회
- **응답 시간 개선**: 50% 이상 단축
- **서버 부하 감소**: CPU/DB 부하 30% 감소

### 코드 품질
- **중복 코드 감소**: 40% 이상
- **유지보수성 향상**: 복잡도 30% 감소
- **테스트 커버리지**: 0% → 80% 이상

### 보안
- **취약점 제거**: Path Traversal 차단
- **안정성 향상**: 데이터 정합성 보장

---

## 권장 작업 순서

1. **즉시 수정** (Critical):
   - 보안 취약점 (Path Traversal)
   - 평균 계산 버그
   - ConnectionService 버그

2. **1개월 내** (High):
   - N+1 쿼리 제거
   - 주요 성능 병목 해결

3. **2개월 내** (Medium):
   - 코드 품질 개선
   - 설계 리팩토링

4. **지속적** (Low + 테스트):
   - 마이너 개선
   - 테스트 코드 작성

---

## 참고 사항

### 리팩토링 시 주의사항
1. **테스트 우선**: 리팩토링 전에 테스트 코드 작성
2. **점진적 개선**: 한 번에 많은 것을 바꾸지 말 것
3. **코드 리뷰**: 팀원과 충분한 논의
4. **롤백 계획**: 문제 발생 시 빠르게 되돌릴 수 있도록

### 성능 측정
리팩토링 전후 성능을 정량적으로 측정하여 개선 효과를 검증하세요.

```java
// JMeter 또는 Gatling으로 부하 테스트
// Spring Boot Actuator로 메트릭 수집
// 쿼리 로깅으로 실제 발생하는 쿼리 수 확인
```

---

## ✅ 해결 완료

### Critical 항목

#### ~~보안 취약점: Path Traversal 공격 가능~~ ✅ (2025-11-01)
- **원래 위치**: `ReviewController.java:166-169`
- **문제**:
  - `imgName` 파라미터에 대한 검증이 전혀 없음
  - `../../etc/passwd` 같은 경로로 서버의 임의 파일 접근 가능
  - 민감한 설정 파일, 소스 코드 노출 위험
  - 위험도: ⚠️ **매우 높음**

- **해결 내용**:
```java
// 수정 전
@GetMapping("/images/{imgName}")
public byte[] showImage(@PathVariable String imgName) throws Exception {
    File file = new File(IMAGE_DIR + "/" + imgName);
    return Files.readAllBytes(file.toPath());
}

// 수정 후
@GetMapping("/images/{imgName}")
public byte[] showImage(@PathVariable String imgName) throws Exception {
    // 파일명 검증 - Path Traversal 공격 방지
    if (imgName.contains("..") || imgName.contains("/") || imgName.contains("\\")) {
        throw new IllegalArgumentException("Invalid image name");
    }

    // UUID 형식 검증 (파일명이 UUID 기반이므로)
    if (!imgName.matches("^[a-f0-9-]+\\.png$")) {
        throw new IllegalArgumentException("Invalid image format");
    }

    Path imagePath = Paths.get(IMAGE_DIR, imgName).normalize();

    // 경로가 IMAGE_DIR 내부에 있는지 확인
    if (!imagePath.startsWith(Paths.get(IMAGE_DIR).toAbsolutePath())) {
        throw new SecurityException("Access denied");
    }

    if (!Files.exists(imagePath)) {
        throw new FileNotFoundException("Image not found");
    }

    return Files.readAllBytes(imagePath);
}
```

- **테스트 작성**: `ImageSecurityTest.java` (11개 테스트 케이스)
  - ✅ 파일명 검증 - 정상적인 UUID 형식
  - ✅ 파일명 검증 - 타임스탬프 포함 UUID 형식
  - ✅ Path Traversal 공격 차단 (`..` 포함)
  - ✅ Path Traversal 공격 차단 (슬래시 포함)
  - ✅ Path Traversal 공격 차단 (백슬래시 포함)
  - ✅ 잘못된 파일 형식 차단 (.jpg)
  - ✅ 잘못된 파일 형식 차단 (.txt)
  - ✅ 특수문자 포함 파일명 차단
  - ✅ 대문자 포함 파일명 차단
  - ✅ 공백 포함 파일명 차단
  - ✅ 복합 공격 패턴 차단

- **커밋**: `fix: prevent path traversal vulnerability in image endpoint`
- **영향**: 서버 보안 강화, 민감한 파일 접근 차단

---

### High 항목

#### ~~비효율적인 정렬: ReviewService.sortReviewsByDate()~~ ✅ (2025-11-01)
- **원래 위치**: `ReviewService.java:103-123`
- **문제**:
  - 매번 문자열을 `LocalDateTime`으로 파싱 (비용이 큼)
  - 불필요한 익명 클래스 사용
  - DateTimeFormatter 매번 생성

- **해결 내용**:
```java
// 수정 전 - 애플리케이션 레벨 정렬
private void sortReviewsByDate(List<Review> reviews) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    Collections.sort(reviews, new Comparator<Review>() {
        @Override
        public int compare(Review o1, Review o2) {
            LocalDateTime o1Time = LocalDateTime.parse(o1.getMadeTime(), formatter);
            LocalDateTime o2Time = LocalDateTime.parse(o2.getMadeTime(), formatter);
            // ... 비교 로직
        }
    });
}

// 수정 후 - DB 레벨 정렬 (sortReviewsByDate 메서드 완전 제거)
// ReviewRepository에 메서드 추가
List<Review> findByRestaurantOrderByMadeTimeDesc(Restaurant restaurant);
List<Review> findByRestaurantAndMenuInOrderByMadeTimeDesc(Restaurant restaurant, List<Menu> menus);

// ReviewService에서 사용
public List<Review> findRestaurantReviews(String restaurantName, String date) {
    Restaurant restaurant = Restaurant.valueOf(Restaurant.parseName(restaurantName));

    if(restaurant.equals(Restaurant.제1학생회관)){
        return reviewRepository.findByRestaurantOrderByMadeTimeDesc(restaurant);
    }

    List<Menu> param = // ... 메뉴 수집 로직
    return reviewRepository.findByRestaurantAndMenuInOrderByMadeTimeDesc(restaurant, param);
    // sortReviewsByDate() 호출 불필요!
}
```

- **테스트 작성**: `ReviewSortingTest.java` (통합 테스트 5개)
  - ✅ findByRestaurantOrderByMadeTimeDesc - 실제 DB 정렬 검증
  - ✅ findByRestaurantAndMenuInOrderByMadeTimeDesc - 실제 DB 정렬 검증
  - ✅ 기존 메서드 vs OrderByMadeTimeDesc - 정렬 차이 확인
  - ✅ 동일 시간 리뷰도 정상 조회
  - ✅ 다른 레스토랑 필터링 확인
  - ✅ **실제 SQL에 `ORDER BY made_time desc` 포함 확인**

- **커밋**: `refactor: optimize review sorting with database-level ordering`
- **영향**:
  - 성능 향상: String 파싱 비용 완전 제거 + DB 인덱스 활용
  - 코드 간결화: 20줄 정렬 로직 제거
  - 확장성: 향후 페이징 처리 용이
  - 메모리 효율: 애플리케이션 레벨 정렬 부하 제거

---

#### ~~데이터 정합성: 잘못된 평균 계산 로직~~ ✅ (2025-11-01)
- **원래 위치**: `Menu.java:127-131`
- **문제**:
  - 수학적으로 잘못된 평균 계산
  - 예시: 현재 평균 4.0 (리뷰 2개), 새 리뷰 5.0 추가
    - 잘못된 계산: (4.0 + 5.0) / 3 = 3.0 ❌
    - 올바른 계산: (4.0 × 2 + 5.0) / 3 = 4.33 ✅
  - 모든 메뉴의 평점이 부정확하게 저장됨

- **해결 내용**:
```java
// 수정 전
public Long addReviewAndUpdateRate(Review review) {
    this.reviewList.add(review);
    this.rate = (this.rate + review.getReviewRate()) / this.reviewList.size();
    return review.getId();
}

// 수정 후
public Long addReviewAndUpdateRate(Review review) {
    this.reviewList.add(review);
    // 올바른 평균 계산: (기존 평균 × 기존 개수 + 새 평점) / 새 개수
    this.rate = (this.rate * (this.reviewList.size() - 1) + review.getReviewRate())
                / this.reviewList.size();
    return review.getId();
}
```

- **테스트 작성**: `MenuRateTest.java`
  - ✅ 첫 리뷰 추가 시 평균 계산
  - ✅ 여러 리뷰 추가 시 올바른 평균
  - ✅ 분석 문서의 예시 케이스 (4.0 평균에 5.0 추가 → 4.33)
  - ✅ 리뷰 삭제 후 평균 재계산
  - ✅ 모든 리뷰 삭제 시 평균 0

- **커밋**: `fix: correct average rating calculation in menu reviews`
- **영향**: 평점 시스템의 정확성 보장, 데이터 정합성 확보

---

**작성자**: Claude Code
**최초 작성**: 2025-11-01
**최종 업데이트**: 2025-11-01
**버전**: 1.1