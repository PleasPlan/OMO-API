package com.OmObe.OmO.Place.service;

import com.OmObe.OmO.Comment.service.CommentService;
import com.OmObe.OmO.Place.entity.Place;
import com.OmObe.OmO.Place.entity.PlaceLike;
import com.OmObe.OmO.Place.entity.PlaceRecommend;
import com.OmObe.OmO.Place.repository.PlaceLikeRepository;
import com.OmObe.OmO.Place.repository.PlaceRecommendRepository;
import com.OmObe.OmO.Place.repository.PlaceRepository;
import com.OmObe.OmO.Review.entity.Review;
import com.OmObe.OmO.Review.repository.ReviewRepository;
import com.OmObe.OmO.auth.jwt.TokenDecryption;
import com.OmObe.OmO.exception.BusinessLogicException;
import com.OmObe.OmO.member.entity.Member;
import com.OmObe.OmO.member.service.MemberService;
import com.OmObe.OmO.util.PairJ;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

@Service
@Slf4j
public class PlaceService {

//    @Value("${naver-map.id}")
//    private String id;
//    @Value("${naver-map.pw}")
//    private String pw;
    @Value("${kakao-map.key}")
    private String key;

    private final PlaceRepository placeRepository;
    private final ReviewRepository reviewRepository;
    private final PlaceLikeRepository placeLikeRepository;
    private final PlaceRecommendRepository placeRecommendRepository;
    private final CommentService commentService;
    private final MemberService memberService;
    private final TokenDecryption tokenDecryption;

    public PlaceService(PlaceRepository placeRepository,
                        ReviewRepository reviewRepository,
                        PlaceLikeRepository placeLikeRepository,
                        PlaceRecommendRepository placeRecommendRepository,
                        CommentService commentService,
                        MemberService memberService,
                        TokenDecryption tokenDecryption) {
        this.placeRepository = placeRepository;
        this.reviewRepository = reviewRepository;
        this.placeLikeRepository = placeLikeRepository;
        this.placeRecommendRepository = placeRecommendRepository;
        this.commentService = commentService;
        this.memberService = memberService;
        this.tokenDecryption = tokenDecryption;
    }

    public String getPlaces(String category, PairJ<Double, Double> middle, int page, String token){
        String keyword;
        try{
            keyword = URLEncoder.encode(category, "UTF-8");
        }catch (UnsupportedEncodingException e){
            throw new RuntimeException("Encoding Failed",e);
        }

        String y = middle.getFirst().toString();
        String x = middle.getSecond().toString();

        String webAddress = "https://dapi.kakao.com/v2/local/search/keyword.json?y="+y+"&x="+x+"&radius=10000&sort=distance&query="+keyword+"&page="+page+"&size=10";

        Map<String, String> requestHeader = new HashMap<>();
//        requestHeader.put("X-Naver-Client-Id", id);
//        requestHeader.put("X-Naver-Client-Secret", pw);
        requestHeader.put("Authorization", "KakaoAK "+key);
        String responseBody = get(webAddress, requestHeader);

        Member member = null;
        if(token != null){
            member = tokenDecryption.getWriterInJWTToken(token);
        }

        responseBody = idTracker(responseBody,member,category);

        return responseBody;
    }

    public String getRecentPlace(List<String> placeNameList, List<Long> placeIdList, Member member){
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode recentPlace = mapper.createArrayNode();
        for(int index = 0; index<placeIdList.size(); index++){
            try {
                recentPlace.add(mapper.readTree(getPlace(placeNameList.get(index),placeIdList.get(index),member)));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        ObjectNode response = mapper.createObjectNode();
        response.set("recentPlace",recentPlace);

        ObjectNode meta = mapper.createObjectNode();
        meta.put("total_count",placeNameList.size());

        try {
            return mapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPlace(String placeName,long placeId,Member member) {

        String keyword;
        try{
            keyword = URLEncoder.encode(placeName, "UTF-8");
        }catch (UnsupportedEncodingException e){
            throw new RuntimeException("Encoding Failed",e);
        }

        String webAddress = "https://dapi.kakao.com/v2/local/search/keyword.json?page=1&query="+keyword;

        Map<String, String> requestHeader = new HashMap<>();
//        requestHeader.put("X-Naver-Client-Id", id);
//        requestHeader.put("X-Naver-Client-Secret", pw);
        requestHeader.put("Authorization", "KakaoAK "+key);
        String responseBody = get(webAddress, requestHeader);

        if(member == null) {
            responseBody = getPlaceComments(responseBody, placeId);
        } else {
            responseBody = getOnePlace(responseBody,placeId,member);
        }


        return responseBody;
    }

    public String getPlace(String placeName,int page,String token) {

        String keyword;
        try{
            keyword = URLEncoder.encode(placeName, "UTF-8");
        }catch (UnsupportedEncodingException e){
            throw new RuntimeException("Encoding Failed",e);
        }

        String webAddress = "https://dapi.kakao.com/v2/local/search/keyword.json?page="+page+"&query="+keyword;

        Map<String, String> requestHeader = new HashMap<>();
//        requestHeader.put("X-Naver-Client-Id", id);
//        requestHeader.put("X-Naver-Client-Secret", pw);
        requestHeader.put("Authorization", "KakaoAK "+key);
        String responseBody = get(webAddress, requestHeader);

        Member member = null;
        if(token != null){
            member = tokenDecryption.getWriterInJWTToken(token);
        }

        responseBody = idTracker(responseBody,member);

        return responseBody;
    }

    private String getOnePlace(String jsonData, long placeId, Member member) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode jsonNode = objectMapper.readTree(jsonData);

            ArrayNode placesNode = jsonNode.get("documents").deepCopy();

            for(int index = 0; index<placesNode.size(); index++){
                ObjectNode objectNode = (ObjectNode) placesNode.get(index);
                long id = placesNode.get(index).get("id").asLong();
                if(placeId == id) {
                    Place place = findPlace(id);
                    boolean mine = false;
                    boolean recommend = false;
                    if(place != null) {
                        List<PlaceLike> placeLikes = place.getPlaceLikeList();
                        List<PlaceRecommend> placeRecommends = place.getPlaceRecommendList();
                        if (!placeLikes.isEmpty()) {
                            for (PlaceLike placeLike : placeLikes) {
                                if (placeLike.getMember() == member) {
                                    mine = true;
                                    break;
                                }
                            }
                        }
                        if (!placeRecommends.isEmpty()) {
                            for (PlaceRecommend placeRecommend : placeRecommends) {
                                if (placeRecommend.getMember() == member) {
                                    recommend = true;
                                    break;
                                }
                            }
                        }

                        /*
                         * ISTP = 0   || 0000
                         * ISTJ = 1   || 0001
                         * ISFP = 2   || 0010
                         * ISFJ = 3   || 0011
                         * INTP = 4   || 0100
                         * INTJ = 5   || 0101
                         * INFP = 6   || 0110
                         * INFJ = 7   || 0111
                         * ESTP = 8   || 1000
                         * ESTJ = 9   || 1001
                         * ESFP = 10  || 1010
                         * ESFJ = 11  || 1011
                         * ENTP = 12  || 1100
                         * ENTJ = 13  || 1101
                         * ENFP = 14  || 1110
                         * ENFJ = 15  || 1111
                         * */
                        if(!place.getPlaceRecommendList().isEmpty()) {
                            int countI = 0;
                            int countS = 0;
                            int countT = 0;
                            int countP = 0;
                            for (int i = 0; i < place.getPlaceRecommendList().size(); i++) {
                                PlaceRecommend placeRecommend = place.getPlaceRecommendList().get(i);
                                int MBTI = placeRecommend.getMember().getMbti();
                                if(MBTI<0){
                                    continue;
                                }
                                String binary = makeMBTIToBinary4Length(MBTI);
                                if (binary.charAt(0) == '0') {
                                    countI++;
                                }
                                if (binary.charAt(1) == '0') {
                                    countS++;
                                }
                                if (binary.charAt(2) == '0') {
                                    countT++;
                                }
                                if (binary.charAt(3) == '0') {
                                    countP++;
                                }
                            }
                            float ratioI = (float) Math.round((float) countI / place.getPlaceRecommendList().size() * 1000) / 1000;
                            float ratioS = (float) Math.round((float) countS / place.getPlaceRecommendList().size() * 1000) / 1000;
                            float ratioT = (float) Math.round((float) countT / place.getPlaceRecommendList().size() * 1000) / 1000;
                            float ratioP = (float) Math.round((float) countP / place.getPlaceRecommendList().size() * 1000) / 1000;

                            objectNode.put("ratioI", ratioI);
                            objectNode.put("ratioS", ratioS);
                            objectNode.put("ratioT", ratioT);
                            objectNode.put("ratioP", ratioP);
                        }

                        objectNode.put("mine", place.getPlaceLikeList().size());
                        objectNode.put("recommend", place.getPlaceRecommendList().size());
                    }   else {
                        objectNode.put("mine", 0);
                        objectNode.put("recommend", 0);
                    }
                    objectNode.put("myMine", mine);
                    objectNode.put("myRecommend", recommend);

                    ArrayNode reviews = JsonNodeFactory.instance.arrayNode();
                    Optional<List<Review>> optionalReviewList = reviewRepository.findByPlaceId(placeId);
                    if (optionalReviewList.isPresent()) {
                        List<Review> reviewList = optionalReviewList.get();
                        for (Review review : reviewList) {
                            ObjectNode reviewNode = objectMapper.createObjectNode();
                            reviewNode.put("writer", review.getMember().getNickname());
                            reviewNode.put("content", review.getContent());
                            reviewNode.put("imageName",review.getImageName());
                            reviews.add(reviewNode);
                        }
                    }
                    objectNode.set("reviews", reviews);

                    JsonNode changedNode = objectNode;
                    placesNode.set(index,changedNode);
                }
                else{
                    NullNode nullNode = NullNode.instance;
                    placesNode.set(index,nullNode);
                }
            }

            Iterator<JsonNode> iterator = placesNode.iterator();
            while (iterator.hasNext()){
                if(iterator.next().isNull()){
                    iterator.remove();
                }
            }

            JsonNode resultNode = placesNode.get(0);
            return objectMapper.writeValueAsString(resultNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String putMineOrRecommend(long placeId, String placeName, Member member, boolean LR){
        Place place = findPlace(placeId);

        String jsonData = getPlace(placeName,placeId,null);
        log.info(jsonData);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode placeNode = objectMapper.readTree(jsonData);
            if(placeNode.get("id").asLong() != placeId){
                return null;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Place newPlace = new Place();
        newPlace.setPlaceId(placeId);
        newPlace.setPlaceName(placeName);

        // Like = true, Recommend = false

        if(place != null){
            if(LR){
                Optional<PlaceLike> optionalPlaceLike = placeLikeRepository.findByMemberAndPlace(member,place);
                if(optionalPlaceLike.isPresent()){
                    PlaceLike existPlaceLike = optionalPlaceLike.orElseThrow();
                    place.deleteLikes(existPlaceLike);
                    member.deletePlaceLikes(existPlaceLike);
                    placeLikeRepository.delete(existPlaceLike);
                } else {
                    PlaceLike like = new PlaceLike();
                    like.setPlace(place);
                    like.setMember(member);
                    place.addLikes(like);
                    member.addPlaceLikes(like);
                    placeLikeRepository.save(like);
                }

            }
            else{
                Optional<PlaceRecommend> optionalPlaceRecommend = placeRecommendRepository.findByMemberAndPlace(member,place);
                if(optionalPlaceRecommend.isPresent()){
                    PlaceRecommend existPlaceRecommend = optionalPlaceRecommend.orElseThrow();
                    place.deleteRecommends(existPlaceRecommend);
                    member.deletePlaceRecommend(existPlaceRecommend);
                    placeRecommendRepository.delete(existPlaceRecommend);
                } else if(reviewRepository.findByMemberAndPlaceId(member,place.getPlaceId()).isPresent()){
                    Review optionalReview = reviewRepository.findByMemberAndPlaceId(member,place.getPlaceId()).get();
                    log.warn("place ID : " + optionalReview.getPlaceId());
                    PlaceRecommend recommend = new PlaceRecommend();
                    recommend.setPlace(place);
                    recommend.setMember(member);
                    place.addRecommends(recommend);
                    member.addPlaceRecommend(recommend);
                    placeRecommendRepository.save(recommend);
                } else {
                    return "No review. Write review first before recommend.";
                }
            }
            placeRepository.save(place);

            return "placeId : "+placeId+"\nmine : "+place.getPlaceLikeList().size()+"\nrecommend : "+place.getPlaceRecommendList().size();
        } else {
            if(LR){
                PlaceLike like = new PlaceLike();
                like.setPlace(place);
                like.setMember(member);
                newPlace.addLikes(like);
                placeLikeRepository.save(like);
            }
            else{
                if(reviewRepository.findByMemberAndPlaceId(member,placeId).isPresent()) {
                    PlaceRecommend recommend = new PlaceRecommend();
                    recommend.setPlace(place);
                    recommend.setMember(member);
                    newPlace.addRecommends(recommend);
                    placeRecommendRepository.save(recommend);
                }
                else{
                    return "No review. Write review first before recommend.";
                }
            }
            placeRepository.save(newPlace);
            return "{\"placeId\":"+newPlace.getPlaceId()+",\"mine\":"+newPlace.getPlaceLikeList().size()+",\"recommend\":"+newPlace.getPlaceRecommendList().size()+"}";
        }
    }

    private static String get(String webAddress, Map<String, String> requestHeader) {
        HttpURLConnection con = connect(webAddress);
        try{
            con.setRequestMethod("GET");
            for(Map.Entry<String, String> header : requestHeader.entrySet()){
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            int responseCode = con.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                return readBody(con.getInputStream());
            } else {
                return readBody(con.getErrorStream());
            }
        } catch (IOException e){
            throw new RuntimeException("API request and response failed", e);
        } finally {
            con.disconnect();
        }
    }

    private static HttpURLConnection connect(String webAddress) {
        try {
            URL url = new URL(webAddress);
            return (HttpURLConnection)url.openConnection();
        }catch (MalformedURLException e){
            throw new RuntimeException("API URL is wrong. : " + webAddress, e);
        }catch (IOException e){
            throw new RuntimeException("Connection Failed. : " + webAddress, e);
        }
    }

    private static String readBody(InputStream body) throws UnsupportedEncodingException {
        InputStreamReader streamReader = new InputStreamReader(body,"UTF-8");

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API response reading failed.", e);
        }
    }

    // 카테고리 별 장소를 찾을 때 사용
    private String idTracker(String jsonData,Member member,String category) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonData);

            JsonNode placesNode = jsonNode.get("documents");

            placesNode = deleteNonCategory((ArrayNode) placesNode,category);

            if(placesNode == NullNode.instance){
                log.warn("Non-exist category");
                return "Non-exist category";
            }

            if(placesNode.isArray()){
                for(JsonNode placeNode : placesNode){
                    long id = placeNode.get("id").asLong();

                    log.info("id : "+id);

                    //PairJ<Place, Boolean> place = findPlaceWithBoolean(id);
                    Place place = findPlace(id);
                    ObjectNode objectNode = (ObjectNode) placeNode;
                    Optional<List<Review>> reviews = reviewRepository.findByPlaceId(id);
                    if(place != null){
                        objectNode.put("mine", place.getPlaceLikeList().size());
                        objectNode.put("recommend", place.getPlaceRecommendList().size());
                    } else {
                        objectNode.put("mine",0);
                        objectNode.put("recommend", 0);
                    }
                    // 멤버 존재 시
                    if(member != null){
                        boolean mine = false;
                        boolean recommend = false;
                        if(place != null) {
                            List<PlaceLike> placeLikes = place.getPlaceLikeList();
                            List<PlaceRecommend> placeRecommends = place.getPlaceRecommendList();
                            if (!placeLikes.isEmpty()) {
                                for (PlaceLike placeLike : placeLikes) {
                                    if (placeLike.getMember() == member) {
                                        mine = true;
                                        break;
                                    }
                                }
                            }
                            if (!placeRecommends.isEmpty()) {
                                for (PlaceRecommend placeRecommend : placeRecommends) {
                                    if (placeRecommend.getMember() == member) {
                                        recommend = true;
                                        break;
                                    }
                                }
                            }
                        }
                        objectNode.put("myMine", mine);
                        objectNode.put("myRecommend", recommend);
                    }

                    boolean imageChecker = true;
                    Collections.reverse(reviews.get());
                    for(Review review: reviews.get()){
                        if(review.getImageName() != null){
                            if(imageChecker) {
                                objectNode.put("firstImageName", review.getImageName());
                                imageChecker = false;
                            } else {
                                objectNode.put("secondImageName", review.getImageName());
                                break;
                            }
                        }
                    }

                    JsonNode changedNode = objectNode;
                    placeNode = changedNode;
                }
            }
            else {
                log.warn("No 'places' array found in the JSON data.");
            }

            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String idTracker(String jsonData,Member member) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonData);

            JsonNode placesNode = jsonNode.get("documents");

            if(placesNode.isArray()){
                for(JsonNode placeNode : placesNode){
                    long id = placeNode.get("id").asLong();

                    log.info("id : "+id);

                    //PairJ<Place, Boolean> place = findPlaceWithBoolean(id);
                    Place place = findPlace(id);
                    ObjectNode objectNode = (ObjectNode) placeNode;
                    Optional<List<Review>> reviews = reviewRepository.findByPlaceId(id);
                    if(place != null){
                        objectNode.put("mine", place.getPlaceLikeList().size());
                        objectNode.put("recommend", place.getPlaceRecommendList().size());
                    } else {
                        objectNode.put("mine",0);
                        objectNode.put("recommend", 0);
                    }
                    // 멤버 존재 시
                    if(member != null){
                        boolean mine = false;
                        boolean recommend = false;
                        if(place != null) {
                            List<PlaceLike> placeLikes = place.getPlaceLikeList();
                            List<PlaceRecommend> placeRecommends = place.getPlaceRecommendList();
                            if (!placeLikes.isEmpty()) {
                                for (PlaceLike placeLike : placeLikes) {
                                    if (placeLike.getMember() == member) {
                                        mine = true;
                                        break;
                                    }
                                }
                            }
                            if (!placeRecommends.isEmpty()) {
                                for (PlaceRecommend placeRecommend : placeRecommends) {
                                    if (placeRecommend.getMember() == member) {
                                        recommend = true;
                                        break;
                                    }
                                }
                            }
                        }
                        objectNode.put("myMine", mine);
                        objectNode.put("myRecommend", recommend);
                    }

                    boolean imageChecker = true;
                    Collections.reverse(reviews.get());
                    for(Review review: reviews.get()){
                        if(review.getImageName() != null){
                            if(imageChecker) {
                                objectNode.put("firstImageName", review.getImageName());
                                imageChecker = false;
                            } else {
                                objectNode.put("secondImageName", review.getImageName());
                                break;
                            }
                        }
                    }

                    JsonNode changedNode = objectNode;
                    placeNode = changedNode;
                }
            }
            else {
                log.warn("No 'places' array found in the JSON data.");
            }

            return objectMapper.writeValueAsString(jsonNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getPlaceComments(String jsonData, long placeId) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode jsonNode = objectMapper.readTree(jsonData);

            ArrayNode placesNode = jsonNode.get("documents").deepCopy();

//            if(placesNode.isArray()){

            for(int index = 0; index<placesNode.size(); index++){
                ObjectNode objectNode = (ObjectNode) placesNode.get(index);
                long id = placesNode.get(index).get("id").asLong();
                if(placeId == id) {
                    log.info("id : " + id);
                    Place place = findPlace(id);
                    if (place!=null) {
                        objectNode.put("mine", place.getPlaceLikeList().size());
                        objectNode.put("recommend", place.getPlaceRecommendList().size());
                        /*
                         * ISTP = 1   || 0000
                         * ISTJ = 2   || 0001
                         * ISFP = 3   || 0010
                         * ISFJ = 4   || 0011
                         * INTP = 5   || 0100
                         * INTJ = 6   || 0101       (-1 ~ 15)
                         * INFP = 7   || 0110
                         * INFJ = 8   || 0111
                         * ESTP = 9   || 1000
                         * ESTJ = 10  || 1001
                         * ESFP = 11  || 1010
                         * ESFJ = 12  || 1011
                         * ENTP = 13  || 1100
                         * ENTJ = 14  || 1101
                         * ENFP = 15  || 1110
                         * ENFJ = 16  || 1111
                         * */
                        if(!place.getPlaceRecommendList().isEmpty()) {
                            int countI = 0;
                            int countS = 0;
                            int countT = 0;
                            int countP = 0;
                            for (int i = 0; i < place.getPlaceRecommendList().size(); i++) {
                                PlaceRecommend placeRecommend = place.getPlaceRecommendList().get(i);
                                int MBTI = placeRecommend.getMember().getMbti();
                                if(MBTI<0){
                                    continue;
                                }
                                String binary = makeMBTIToBinary4Length(MBTI);
                                if (binary.charAt(0) == '0') {
                                    countI++;
                                }
                                if (binary.charAt(1) == '0') {
                                    countS++;
                                }
                                if (binary.charAt(2) == '0') {
                                    countT++;
                                }
                                if (binary.charAt(3) == '0') {
                                    countP++;
                                }
                            }
                            float ratioI = (float) Math.round((float) countI / place.getPlaceRecommendList().size() * 1000) / 1000;
                            float ratioS = (float) Math.round((float) countS / place.getPlaceRecommendList().size() * 1000) / 1000;
                            float ratioT = (float) Math.round((float) countT / place.getPlaceRecommendList().size() * 1000) / 1000;
                            float ratioP = (float) Math.round((float) countP / place.getPlaceRecommendList().size() * 1000) / 1000;
                            objectNode.put("ratioI", ratioI);
                            objectNode.put("ratioS", ratioS);
                            objectNode.put("ratioT", ratioT);
                            objectNode.put("ratioP", ratioP);
                        }
                    } else {
                        objectNode.put("mine", 0);
                        objectNode.put("recommend", 0);
                    }
                    ArrayNode reviews = JsonNodeFactory.instance.arrayNode();
                    Optional<List<Review>> optionalReviewList = reviewRepository.findByPlaceId(placeId);
                    if (optionalReviewList.isPresent()) {
                        List<Review> reviewList = optionalReviewList.get();
                        for (Review review : reviewList) {
                            ObjectNode reviewNode = objectMapper.createObjectNode();
                            reviewNode.put("writer", review.getMember().getNickname());
                            reviewNode.put("content", review.getContent());
                            // TODO: 이미지 넣어야 됨.
                            reviews.add(reviewNode);
                        }
                    }
                    objectNode.set("reviews", reviews);

                    JsonNode changedNode = objectNode;
                    placesNode.set(index,changedNode);
                }
                else{
                    NullNode nullNode = NullNode.instance;
                    placesNode.set(index,nullNode);
                }
            }

            Iterator<JsonNode> iterator = placesNode.iterator();
            while (iterator.hasNext()){
                if(iterator.next().isNull()){
                    iterator.remove();
                }
            }

            JsonNode resultNode = placesNode.get(0);
            return objectMapper.writeValueAsString(resultNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Place findPlace(long placeId){
        Optional<Place> optionalPlace = placeRepository.findById(placeId);
        if(optionalPlace.isEmpty()){
            return null;
        }else {
            Place place = optionalPlace.orElseThrow();
            return place;
        }
    }

    private static String makeMBTIToBinary4Length(int MBTI){
        String binaryMBTI = Integer.toBinaryString(MBTI);
//        StringBuilder binary = new StringBuilder("0000");
//        for(int indexMBTI = 0; indexMBTI<binaryMBTI.length(); indexMBTI++){
//            binary.replace(indexMBTI,indexMBTI+1,binaryMBTI.substring(indexMBTI,indexMBTI+1));
//        }
//        return binary.toString();
        StringBuilder binary = new StringBuilder(binaryMBTI);
        while(binary.length()<4){
            binary = new StringBuilder("0" + binary);
        }
        return binary.toString();
    }

    private JsonNode deleteNonCategory(ArrayNode placesNode, String category) {
        String categoryName = null;
        switch (category) {
            // 놀거리
            case "테마파크": {
                categoryName = "여행 > 관광,명소 > 테마파크";
                break;
            }
            case "축제": {
                categoryName = "여행 > 축제";
                break;
            }
            case "오락실": {
                categoryName = "가정,생활 > 여가시설 > 오락실";
                break;
            }
            case "팝업스토어": {
                // 그냥 이름으로만 찾아도 될 듯
                break;
            }
            case "레저": {
                categoryName = "스포츠,레저";
                break;
            }
            case "쇼핑몰": {
                // 뭐든 팔면 정녕 쇼핑몰이라고 할 수 있는가
                break;
            }

            // 먹거리
            case "한식": {
                categoryName = "음식점 > 한식";
                break;
            }
            case "중식": {
                categoryName = "음식점 > 중식";
                break;
            }
            case "일식": {
                categoryName = "음식점 > 일식";
                break;
            }
            case "양식": {
                categoryName = "음식점 > 양식";
                break;
            }
            case "치킨": {
                categoryName = "음식점 > 치킨";
                break;
            }
            case "패스트푸드": {
                categoryName = "음식점 > 패스트푸드";
                break;
            }

            // 볼거리
            case "영화관": {
                categoryName = "문화,예술 > 영화,영상 > 영화관";
                break;
            }
            case "박물관": {
                categoryName = "문화,예술 > 문화시설 > 박물관";
                break;
            }
            case "수족관": {
                categoryName = "문화,예술 > 문화시설 > 아쿠아리움";
                break;
            }
            case "전시관": {
                categoryName = "문화,예술 > 문화시설 > 전시관";
                break;
            }
            case "과학관": {
                categoryName = "문화,예술 > 문화시설 > 과학관";
                break;
            }
            case "미술관": {
                categoryName = "문화,예술 > 문화시설 > 미술관";
                break;
            }
            case "카페": {
                categoryName = "음식점 > 카페";
                break;
            }

            case "룸카페": {
                // 이름에 롬카페가 들어가면 ok인듯;
                break;
            }
            case "방탈출": {
                categoryName = "가정,생활 > 여가시설 > 방탈출카페";
                break;
            }
            case "만화카페": {
                categoryName = "가정,생활 > 여가시설 > 만화방 > 만화카페";
                break;
            }
            case "동물원": {
                categoryName = "여행 > 관광,명소 > 동물원";
                // categoryName = "음식점 > 카페 > 테마카페 > 애견카페";
                // categoryName = "음식점 > 카페 > 테마카페 > 고양이카페";
                break;
            }
            case "보드게임": {
                categoryName = "가정,생활 > 여가시설 > 보드카페";
                break;
            }
            case "공방": {
                // 그냥 공방카페라고 검색하면 됨.
                break;
            }
            default:{
                return NullNode.instance;
            }
        }

        try {
            if(categoryName != null) {
                for (int index = 0; index < placesNode.size(); index++) {
                    ObjectNode objectNode = (ObjectNode) placesNode.get(index);
                    String nodeCategoryName = objectNode.get("category_name").asText();
                    if (nodeCategoryName.contains(categoryName)) {
                        JsonNode changedNode = objectNode;
                        placesNode.set(index, changedNode);
                    } else {
                        NullNode nullNode = NullNode.instance;
                        placesNode.set(index, nullNode);
                    }
                }

                Iterator<JsonNode> iterator = placesNode.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().isNull()) {
                        iterator.remove();
                    }
                }
            }

            return placesNode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
