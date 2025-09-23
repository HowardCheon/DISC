package com.disc.util;

import com.disc.dao.UserDAO;
import com.disc.dao.TestLinkDAO;
import com.disc.dao.AnswerDAO;
import com.disc.dao.ResultDAO;
import com.disc.model.User;
import com.disc.model.TestLink;
import com.disc.model.Answer;
import com.disc.model.Result;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * 테스트용 샘플 데이터 생성 유틸리티
 */
public class SampleDataGenerator {

    private static final Logger logger = Logger.getLogger(SampleDataGenerator.class.getName());

    // 샘플 사용자 이름들
    private static final String[] SAMPLE_NAMES = {
        "김철수", "이영희", "박민준", "최지은", "정현우",
        "강미영", "윤도현", "임소진", "조성민", "한예린",
        "배준호", "신유리", "오태웅", "장나영", "홍길동"
    };

    // DISC 질문별 샘플 응답 패턴
    private static final Map<String, int[]> DISC_PATTERNS = new HashMap<String, int[]>() {{
        // D형 (주도형) 패턴
        put("D", new int[]{4, 2, 1, 3, 4, 1, 2, 3, 4, 2, 1, 3, 4, 1, 2, 3, 4, 2, 1, 3, 4, 1, 2, 3, 4, 2, 1, 3});
        // I형 (사교형) 패턴
        put("I", new int[]{2, 4, 3, 1, 2, 4, 3, 1, 2, 4, 3, 1, 2, 4, 3, 1, 2, 4, 3, 1, 2, 4, 3, 1, 2, 4, 3, 1});
        // S형 (안정형) 패턴
        put("S", new int[]{1, 3, 4, 2, 1, 3, 4, 2, 1, 3, 4, 2, 1, 3, 4, 2, 1, 3, 4, 2, 1, 3, 4, 2, 1, 3, 4, 2});
        // C형 (신중형) 패턴
        put("C", new int[]{3, 1, 2, 4, 3, 1, 2, 4, 3, 1, 2, 4, 3, 1, 2, 4, 3, 1, 2, 4, 3, 1, 2, 4, 3, 1, 2, 4});
        // 혼합형 패턴
        put("Mixed", new int[]{3, 3, 2, 2, 3, 3, 2, 2, 3, 3, 2, 2, 3, 3, 2, 2, 3, 3, 2, 2, 3, 3, 2, 2, 3, 3, 2, 2});
    }};

    private UserDAO userDAO;
    private TestLinkDAO testLinkDAO;
    private AnswerDAO answerDAO;
    private ResultDAO resultDAO;

    public SampleDataGenerator() throws SQLException {
        this.userDAO = new UserDAO();
        this.testLinkDAO = new TestLinkDAO();
        this.answerDAO = new AnswerDAO();
        this.resultDAO = new ResultDAO();
    }

    /**
     * 샘플 데이터 생성 메인 메서드
     */
    public void generateSampleData() {
        try {
            logger.info("샘플 데이터 생성 시작...");

            // 1. 기존 샘플 데이터 정리
            cleanupExistingData();

            // 2. 사용자 및 테스트 링크 생성
            List<User> users = createSampleUsers();

            // 3. 일부 사용자는 검사 완료 상태로 설정
            completeTestsForSomeUsers(users);

            logger.info("샘플 데이터 생성 완료!");
            printSummary();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "샘플 데이터 생성 중 오류 발생", e);
        }
    }

    /**
     * 기존 샘플 데이터 정리
     */
    private void cleanupExistingData() throws SQLException {
        logger.info("기존 샘플 데이터 정리 중...");

        // 샘플 데이터만 삭제 (실제 데이터는 보존)
        for (String name : SAMPLE_NAMES) {
            User user = userDAO.getUserByName(name);
            if (user != null) {
                // 해당 사용자의 모든 데이터 삭제
                List<TestLink> userLinks = testLinkDAO.getTestLinksByUserId(user.getId());
                for (TestLink link : userLinks) {
                    // 결과 삭제
                    Result result = resultDAO.getResultByTestLinkId(link.getId());
                    if (result != null) {
                        resultDAO.deleteResult(result.getId());
                    }

                    // 답변 삭제
                    answerDAO.deleteAnswersByTestLinkId(link.getId());

                    // 테스트 링크 삭제
                    testLinkDAO.deleteTestLink(link.getId());
                }

                // 사용자 삭제
                userDAO.deleteUser(user.getId());
            }
        }
    }

    /**
     * 샘플 사용자 생성
     */
    private List<User> createSampleUsers() throws SQLException {
        logger.info("샘플 사용자 생성 중...");

        List<User> users = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            String userName = SAMPLE_NAMES[i];

            // 사용자 생성
            User user = userDAO.findOrCreateUser(userName);
            users.add(user);

            // 테스트 링크 생성
            String uniqueUrl = generateUniqueUrl();
            TestLink.Status status = (i < 7) ? TestLink.Status.검사완료 :
                                   (i < 8) ? TestLink.Status.진행중 : TestLink.Status.대기중;

            Timestamp createdAt = generateRandomTimestamp(-30, 0); // 최근 30일 내
            Timestamp startedAt = (status != TestLink.Status.대기중) ?
                                 generateRandomTimestamp(-25, -1) : null;
            Timestamp completedAt = (status == TestLink.Status.검사완료) ?
                                  generateRandomTimestamp(-20, -1) : null;

            TestLink testLink = new TestLink(
                user.getId(),
                uniqueUrl,
                status,
                createdAt,
                startedAt,
                completedAt
            );

            testLink = testLinkDAO.createTestLink(testLink);

            logger.info(String.format("사용자 생성: %s (상태: %s)", userName, status));
        }

        return users;
    }

    /**
     * 일부 사용자의 검사 완료 처리
     */
    private void completeTestsForSomeUsers(List<User> users) throws SQLException {
        logger.info("샘플 검사 결과 생성 중...");

        String[] discTypes = {"D", "I", "S", "C", "Mixed"};
        Random random = new Random();

        for (int i = 0; i < 7; i++) { // 처음 7명은 검사 완료
            User user = users.get(i);
            List<TestLink> userLinks = testLinkDAO.getTestLinksByUserId(user.getId());

            if (!userLinks.isEmpty()) {
                TestLink testLink = userLinks.get(0);
                String discType = discTypes[i % discTypes.length];

                // 답변 생성
                createSampleAnswers(testLink.getId(), discType);

                // 결과 생성
                createSampleResult(testLink.getId(), discType);
            }
        }
    }

    /**
     * 샘플 답변 생성
     */
    private void createSampleAnswers(Long testLinkId, String discType) throws SQLException {
        int[] pattern = DISC_PATTERNS.get(discType);
        Random random = new Random();

        for (int questionNum = 1; questionNum <= 28; questionNum++) {
            int baseAnswer = pattern[questionNum - 1];

            // 약간의 랜덤성 추가 (±1)
            int finalAnswer = Math.max(1, Math.min(4, baseAnswer + random.nextInt(3) - 1));

            Answer answer = new Answer(
                testLinkId,
                questionNum,
                finalAnswer,
                new Timestamp(System.currentTimeMillis())
            );

            answerDAO.createAnswer(answer);
        }
    }

    /**
     * 샘플 결과 생성
     */
    private void createSampleResult(Long testLinkId, String discType) throws SQLException {
        Random random = new Random();

        // DISC 점수 생성 (선택된 타입이 높은 점수를 갖도록)
        Map<String, Integer> scores = new HashMap<>();

        switch (discType) {
            case "D":
                scores.put("D", 85 + random.nextInt(15));
                scores.put("I", 20 + random.nextInt(30));
                scores.put("S", 15 + random.nextInt(25));
                scores.put("C", 25 + random.nextInt(20));
                break;
            case "I":
                scores.put("D", 25 + random.nextInt(20));
                scores.put("I", 80 + random.nextInt(20));
                scores.put("S", 30 + random.nextInt(25));
                scores.put("C", 20 + random.nextInt(20));
                break;
            case "S":
                scores.put("D", 15 + random.nextInt(20));
                scores.put("I", 30 + random.nextInt(25));
                scores.put("S", 85 + random.nextInt(15));
                scores.put("C", 35 + random.nextInt(20));
                break;
            case "C":
                scores.put("D", 20 + random.nextInt(25));
                scores.put("I", 25 + random.nextInt(20));
                scores.put("S", 30 + random.nextInt(25));
                scores.put("C", 80 + random.nextInt(20));
                break;
            default: // Mixed
                scores.put("D", 50 + random.nextInt(20));
                scores.put("I", 55 + random.nextInt(20));
                scores.put("S", 45 + random.nextInt(20));
                scores.put("C", 50 + random.nextInt(20));
                break;
        }

        // 주요 유형 결정
        String primaryType = scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .get().getKey();

        Result result = new Result(
            testLinkId,
            scores.get("D"),
            scores.get("I"),
            scores.get("S"),
            scores.get("C"),
            primaryType,
            new Timestamp(System.currentTimeMillis())
        );

        resultDAO.createResult(result);
    }

    /**
     * 유니크 URL 생성
     */
    private String generateUniqueUrl() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 랜덤 타임스탬프 생성
     */
    private Timestamp generateRandomTimestamp(int minDaysAgo, int maxDaysAgo) {
        Random random = new Random();
        long now = System.currentTimeMillis();
        long minTime = now + (minDaysAgo * 24L * 60 * 60 * 1000);
        long maxTime = now + (maxDaysAgo * 24L * 60 * 60 * 1000);

        long randomTime = minTime + (long) (random.nextDouble() * (maxTime - minTime));
        return new Timestamp(randomTime);
    }

    /**
     * 생성된 데이터 요약 출력
     */
    private void printSummary() {
        try {
            long totalUsers = userDAO.getUserCount();
            long totalLinks = testLinkDAO.getTestLinkCount();
            long completedTests = testLinkDAO.getCompletedTestCount();

            System.out.println("\n=== 샘플 데이터 생성 완료 ===");
            System.out.println("총 사용자 수: " + totalUsers);
            System.out.println("총 테스트 링크 수: " + totalLinks);
            System.out.println("완료된 검사 수: " + completedTests);
            System.out.println("완료율: " + String.format("%.1f%%", (completedTests * 100.0 / totalLinks)));
            System.out.println("=============================\n");

        } catch (SQLException e) {
            logger.log(Level.WARNING, "요약 정보 조회 중 오류", e);
        }
    }

    /**
     * 메인 메서드 - 독립 실행용
     */
    public static void main(String[] args) {
        try {
            // 데이터베이스 초기화
            DBUtil.initialize(null);

            // 샘플 데이터 생성
            SampleDataGenerator generator = new SampleDataGenerator();
            generator.generateSampleData();

        } catch (Exception e) {
            logger.log(Level.SEVERE, "샘플 데이터 생성 실패", e);
            System.exit(1);
        }
    }
}