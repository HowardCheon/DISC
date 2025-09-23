package com.disc.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;

/**
 * DISC personality type descriptions and characteristics
 */
public class DiscTypeDescriptions {
    
    /**
     * Get detailed description for a DISC type
     * 
     * @param type DISC type (D, I, S, C)
     * @return Map containing detailed information about the type
     */
    public static Map<String, Object> getTypeDescription(String type) {
        Map<String, Object> description = new HashMap<>();
        
        switch (type.toUpperCase()) {
            case "D":
                description.put("name", "주도형 (Dominance)");
                description.put("subtitle", "도전하는 리더");
                description.put("emoji", "💪");
                description.put("color", "#e74c3c");
                description.put("characteristics", 
                    "결과 지향적이고 결단력이 있으며, 도전을 두려워하지 않습니다. " +
                    "빠른 결정을 내리고 목표 달성에 집중하며, 위험을 감수하고라도 " +
                    "새로운 기회를 추구하는 적극적인 성향을 가지고 있습니다.");
                
                description.put("strengths", Arrays.asList(
                    "강한 리더십과 추진력",
                    "빠른 의사결정 능력",
                    "도전 정신과 혁신적 사고",
                    "결과 중심적 업무 처리",
                    "위기 상황에서의 뛰어난 대응력",
                    "목표 지향적 사고",
                    "변화를 주도하는 능력"
                ));
                
                description.put("developmentAreas", Arrays.asList(
                    "타인의 의견을 경청하는 능력",
                    "세부사항에 대한 주의 깊은 검토",
                    "인내심과 참을성 기르기",
                    "팀원들과의 협력적 소통",
                    "감정적 배려와 공감 능력",
                    "완벽주의보다는 과정 중시"
                ));
                
                description.put("communicationStyle", 
                    "직접적이고 간결한 소통을 선호하며, 핵심을 빠르게 파악하고 전달합니다. " +
                    "목적과 결과에 집중한 대화를 좋아하고, 불필요한 설명보다는 요점 정리를 중시합니다.");
                
                description.put("workEnvironment", 
                    "자율성이 보장되고 도전적인 업무를 할 수 있는 환경을 선호합니다. " +
                    "빠른 변화와 혁신이 가능한 조직에서 역량을 발휘하며, " +
                    "권한과 책임이 명확한 업무를 좋아합니다.");
                
                description.put("stressFactors", Arrays.asList(
                    "지나친 세부사항 요구",
                    "느린 의사결정 과정",
                    "권한 없는 책임",
                    "반복적이고 단조로운 업무",
                    "과도한 규칙과 절차"
                ));
                
                description.put("motivators", Arrays.asList(
                    "도전적인 목표와 과제",
                    "자율적인 업무 환경",
                    "성과에 대한 인정",
                    "승진과 성장 기회",
                    "권한과 영향력 확대"
                ));
                break;
                
            case "I":
                description.put("name", "사교형 (Influence)");
                description.put("subtitle", "영감을 주는 소통가");
                description.put("emoji", "🌟");
                description.put("color", "#f39c12");
                description.put("characteristics", 
                    "사람들과의 관계를 중시하고 긍정적이며 활발합니다. " +
                    "팀워크를 통해 목표를 달성하고 주변 사람들에게 영감을 주며, " +
                    "창의적이고 혁신적인 아이디어로 조직에 활력을 불어넣습니다.");
                
                description.put("strengths", Arrays.asList(
                    "뛰어난 대인관계 능력",
                    "긍정적이고 밝은 에너지",
                    "팀 동기부여와 영감 제공",
                    "창의적이고 혁신적인 아이디어",
                    "변화에 대한 적응력",
                    "설득력 있는 커뮤니케이션",
                    "네트워킹과 관계 구축 능력"
                ));
                
                description.put("developmentAreas", Arrays.asList(
                    "세부사항에 대한 체계적 관리",
                    "일관성 있는 업무 수행",
                    "객관적 분석과 비판적 사고",
                    "시간 관리와 우선순위 설정",
                    "혼자 집중해서 하는 업무 능력",
                    "데이터와 사실 기반 의사결정"
                ));
                
                description.put("communicationStyle", 
                    "열정적이고 표현력이 풍부한 소통을 합니다. " +
                    "사람들과의 관계를 통해 정보를 교환하고 감정적 연결을 중시하며, " +
                    "스토리텔링과 비유를 활용한 설득력 있는 대화를 선호합니다.");
                
                description.put("workEnvironment", 
                    "사람들과 함께 일할 수 있고 창의성을 발휘할 수 있는 환경을 선호합니다. " +
                    "자유로운 분위기와 다양한 사람들과의 교류가 가능한 조직을 좋아하며, " +
                    "인정과 칭찬이 많은 문화를 중시합니다.");
                
                description.put("stressFactors", Arrays.asList(
                    "혼자 하는 반복적 업무",
                    "지나친 세부사항 요구",
                    "사람들과의 단절",
                    "비판적이고 부정적 환경",
                    "엄격한 규칙과 절차"
                ));
                
                description.put("motivators", Arrays.asList(
                    "팀워크와 협업 기회",
                    "창의적 프로젝트 참여",
                    "공개적 인정과 칭찬",
                    "다양한 사람들과의 교류",
                    "자유로운 업무 환경"
                ));
                break;
                
            case "S":
                description.put("name", "안정형 (Steadiness)");
                description.put("subtitle", "신뢰할 수 있는 조력자");
                description.put("emoji", "🤝");
                description.put("color", "#27ae60");
                description.put("characteristics", 
                    "안정성과 조화를 중시하며 꾸준하고 신뢰할 수 있습니다. " +
                    "팀의 화합을 도모하고 일관성 있는 성과를 만들어내며, " +
                    "다른 사람들을 지원하고 협력하는 것을 중요하게 생각합니다.");
                
                description.put("strengths", Arrays.asList(
                    "높은 신뢰성과 일관성",
                    "팀 화합과 협력 촉진",
                    "차분하고 안정적인 업무 처리",
                    "타인에 대한 배려와 지원",
                    "지속적이고 꾸준한 노력",
                    "갈등 조정과 중재 능력",
                    "충성심과 헌신적 태도"
                ));
                
                description.put("developmentAreas", Arrays.asList(
                    "변화에 대한 적응력 향상",
                    "적극적인 의견 표현",
                    "새로운 도전에 대한 개방성",
                    "빠른 의사결정 능력",
                    "자기주장과 리더십 개발",
                    "혁신과 창의성 발휘"
                ));
                
                description.put("communicationStyle", 
                    "차분하고 경청 중심의 소통을 합니다. " +
                    "상대방의 입장을 이해하려 노력하고 갈등을 피하는 평화로운 대화를 선호하며, " +
                    "신중하고 배려 깊은 표현을 사용합니다.");
                
                description.put("workEnvironment", 
                    "안정적이고 예측 가능한 업무 환경을 선호합니다. " +
                    "팀워크가 중시되고 점진적인 변화가 이루어지는 조직에서 최고의 성과를 발휘하며, " +
                    "상호 지원과 협력이 활발한 문화를 좋아합니다.");
                
                description.put("stressFactors", Arrays.asList(
                    "갑작스러운 변화와 압박",
                    "갈등이 많은 환경",
                    "불확실한 미래",
                    "개인적 대립 상황",
                    "지나친 경쟁 문화"
                ));
                
                description.put("motivators", Arrays.asList(
                    "안정적인 업무 환경",
                    "팀원들과의 화합",
                    "점진적 성장과 발전",
                    "타인을 돕는 기회",
                    "상호 신뢰와 존중"
                ));
                break;
                
            case "C":
                description.put("name", "신중형 (Conscientiousness)");
                description.put("subtitle", "완벽을 추구하는 분석가");
                description.put("emoji", "🔍");
                description.put("color", "#3498db");
                description.put("characteristics", 
                    "정확성과 품질을 중시하며 체계적이고 분석적입니다. " +
                    "완벽한 결과를 위해 세심한 검토와 계획을 통해 업무를 수행하며, " +
                    "높은 기준과 전문성을 바탕으로 신뢰할 수 있는 결과물을 만들어냅니다.");
                
                description.put("strengths", Arrays.asList(
                    "뛰어난 분석력과 문제 해결 능력",
                    "높은 품질과 정확성 추구",
                    "체계적이고 계획적인 업무 처리",
                    "객관적이고 논리적인 판단",
                    "전문성과 기술적 역량",
                    "철저한 준비와 검토",
                    "규칙과 절차 준수"
                ));
                
                description.put("developmentAreas", Arrays.asList(
                    "빠른 의사결정과 실행력",
                    "유연성과 적응력 향상",
                    "대인관계와 소통 능력",
                    "완벽주의 성향 조절",
                    "위험 감수와 도전 정신",
                    "감정적 표현과 공감 능력"
                ));
                
                description.put("communicationStyle", 
                    "사실과 데이터에 기반한 논리적 소통을 선호합니다. " +
                    "구체적이고 정확한 정보 전달을 중시하며 신중한 대화를 하고, " +
                    "근거가 명확한 체계적인 설명을 좋아합니다.");
                
                description.put("workEnvironment", 
                    "전문성을 발휘할 수 있고 품질을 중시하는 환경을 선호합니다. " +
                    "체계적인 프로세스와 명확한 기준이 있는 조직에서 역량을 최대화하며, " +
                    "충분한 시간과 자원이 주어지는 업무 환경을 좋아합니다.");
                
                description.put("stressFactors", Arrays.asList(
                    "시간 부족과 촉박한 마감",
                    "불명확한 지시사항",
                    "품질을 타협해야 하는 상황",
                    "갑작스러운 변경 요청",
                    "근거 없는 의사결정"
                ));
                
                description.put("motivators", Arrays.asList(
                    "전문성 개발 기회",
                    "품질 높은 결과물 산출",
                    "체계적인 업무 환경",
                    "충분한 준비 시간",
                    "정확성에 대한 인정"
                ));
                break;
                
            default:
                description.put("name", "알 수 없는 유형");
                description.put("characteristics", "유형 정보를 찾을 수 없습니다.");
        }
        
        return description;
    }
    
    /**
     * Get all DISC type descriptions
     * 
     * @return Map of all DISC types with their descriptions
     */
    public static Map<String, Map<String, Object>> getAllTypeDescriptions() {
        Map<String, Map<String, Object>> allDescriptions = new HashMap<>();
        
        allDescriptions.put("D", getTypeDescription("D"));
        allDescriptions.put("I", getTypeDescription("I"));
        allDescriptions.put("S", getTypeDescription("S"));
        allDescriptions.put("C", getTypeDescription("C"));
        
        return allDescriptions;
    }
    
    /**
     * Get career recommendations for a DISC type
     * 
     * @param type DISC type
     * @return List of career recommendations
     */
    public static List<String> getCareerRecommendations(String type) {
        switch (type.toUpperCase()) {
            case "D":
                return Arrays.asList(
                    "CEO, 경영진",
                    "영업 관리자",
                    "프로젝트 매니저",
                    "컨설턴트",
                    "기업가, 창업자",
                    "변호사",
                    "정치인"
                );
            case "I":
                return Arrays.asList(
                    "마케팅 전문가",
                    "홍보 담당자",
                    "영업 담당자",
                    "HR 전문가",
                    "교육 트레이너",
                    "방송인, 연예인",
                    "이벤트 기획자"
                );
            case "S":
                return Arrays.asList(
                    "간호사, 의료진",
                    "교사, 교육자",
                    "사회복지사",
                    "고객서비스 담당자",
                    "행정 담당자",
                    "카운슬러",
                    "팀 조정자"
                );
            case "C":
                return Arrays.asList(
                    "회계사, 재무 전문가",
                    "엔지니어",
                    "연구원, 과학자",
                    "품질관리 담당자",
                    "데이터 분석가",
                    "IT 전문가",
                    "감사원"
                );
            default:
                return Arrays.asList("추천 직업 정보가 없습니다.");
        }
    }
    
    /**
     * Get type compatibility information
     * 
     * @param type DISC type
     * @return Map containing compatibility information
     */
    public static Map<String, Object> getTypeCompatibility(String type) {
        Map<String, Object> compatibility = new HashMap<>();
        
        switch (type.toUpperCase()) {
            case "D":
                compatibility.put("bestWith", Arrays.asList("I", "S"));
                compatibility.put("challengingWith", Arrays.asList("D", "C"));
                compatibility.put("workingStyle", "독립적이고 결과 지향적");
                break;
            case "I":
                compatibility.put("bestWith", Arrays.asList("D", "S"));
                compatibility.put("challengingWith", Arrays.asList("C"));
                compatibility.put("workingStyle", "협력적이고 관계 지향적");
                break;
            case "S":
                compatibility.put("bestWith", Arrays.asList("D", "I", "C"));
                compatibility.put("challengingWith", Arrays.asList());
                compatibility.put("workingStyle", "지원적이고 안정 지향적");
                break;
            case "C":
                compatibility.put("bestWith", Arrays.asList("S"));
                compatibility.put("challengingWith", Arrays.asList("D", "I"));
                compatibility.put("workingStyle", "분석적이고 품질 지향적");
                break;
        }
        
        return compatibility;
    }
}