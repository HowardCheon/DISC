// DISC 질문 데이터
const DISC_QUESTIONS = [
    {
        id: 1,
        options: [
            { text: "모험적이다", type: "D" },
            { text: "적응성이 뛰어나다", type: "I" },
            { text: "활기찬", type: "I" },
            { text: "분석적이다", type: "C" }
        ]
    },
    {
        id: 2,
        options: [
            { text: "끈질기다", type: "D" },
            { text: "재미있다", type: "I" },
            { text: "부드럽다", type: "S" },
            { text: "신중하다", type: "C" }
        ]
    },
    {
        id: 3,
        options: [
            { text: "경쟁적이다", type: "D" },
            { text: "친근하다", type: "I" },
            { text: "지지적이다", type: "S" },
            { text: "정확하다", type: "C" }
        ]
    },
    {
        id: 4,
        options: [
            { text: "대담하다", type: "D" },
            { text: "마음이 따뜻하다", type: "S" },
            { text: "매력적이다", type: "I" },
            { text: "일관성이 있다", type: "C" }
        ]
    },
    {
        id: 5,
        options: [
            { text: "자발적이다", type: "D" },
            { text: "동정심이 많다", type: "S" },
            { text: "설득력이 있다", type: "I" },
            { text: "보수적이다", type: "C" }
        ]
    },
    {
        id: 6,
        options: [
            { text: "결단력이 있다", type: "D" },
            { text: "친절하다", type: "S" },
            { text: "매혹적이다", type: "I" },
            { text: "신중하다", type: "C" }
        ]
    },
    {
        id: 7,
        options: [
            { text: "자신감이 있다", type: "D" },
            { text: "좋은 청취자다", type: "S" },
            { text: "활발하다", type: "I" },
            { text: "꼼꼼하다", type: "C" }
        ]
    },
    {
        id: 8,
        options: [
            { text: "독립적이다", type: "D" },
            { text: "만족스럽다", type: "S" },
            { text: "사회적이다", type: "I" },
            { text: "완벽주의자다", type: "C" }
        ]
    },
    {
        id: 9,
        options: [
            { text: "의지가 강하다", type: "D" },
            { text: "수용적이다", type: "S" },
            { text: "설득력이 있다", type: "I" },
            { text: "정확하다", type: "C" }
        ]
    },
    {
        id: 10,
        options: [
            { text: "성취지향적이다", type: "D" },
            { text: "친근하다", type: "S" },
            { text: "사교적이다", type: "I" },
            { text: "신중하다", type: "C" }
        ]
    },
    {
        id: 11,
        options: [
            { text: "진취적이다", type: "D" },
            { text: "인내심이 있다", type: "S" },
            { text: "감동적이다", type: "I" },
            { text: "정확하다", type: "C" }
        ]
    },
    {
        id: 12,
        options: [
            { text: "힘있다", type: "D" },
            { text: "평화로운", type: "S" },
            { text: "긍정적이다", type: "I" },
            { text: "체계적이다", type: "C" }
        ]
    },
    {
        id: 13,
        options: [
            { text: "도전적이다", type: "D" },
            { text: "충성스럽다", type: "S" },
            { text: "활기찬", type: "I" },
            { text: "정확하다", type: "C" }
        ]
    },
    {
        id: 14,
        options: [
            { text: "확신에 찬", type: "D" },
            { text: "안정적이다", type: "S" },
            { text: "영감을 준다", type: "I" },
            { text: "신중하다", type: "C" }
        ]
    },
    {
        id: 15,
        options: [
            { text: "결정적이다", type: "D" },
            { text: "온화하다", type: "S" },
            { text: "낙관적이다", type: "I" },
            { text: "체계적이다", type: "C" }
        ]
    },
    {
        id: 16,
        options: [
            { text: "모험을 즐긴다", type: "D" },
            { text: "관대하다", type: "S" },
            { text: "표현력이 풍부하다", type: "I" },
            { text: "정확하다", type: "C" }
        ]
    },
    {
        id: 17,
        options: [
            { text: "강인하다", type: "D" },
            { text: "관용적이다", type: "S" },
            { text: "생동감 있다", type: "I" },
            { text: "정확하다", type: "C" }
        ]
    },
    {
        id: 18,
        options: [
            { text: "지배적이다", type: "D" },
            { text: "친절하다", type: "S" },
            { text: "감정 표현이 풍부하다", type: "I" },
            { text: "객관적이다", type: "C" }
        ]
    },
    {
        id: 19,
        options: [
            { text: "원동력이 있다", type: "D" },
            { text: "동정적이다", type: "S" },
            { text: "열정적이다", type: "I" },
            { text: "체계적이다", type: "C" }
        ]
    },
    {
        id: 20,
        options: [
            { text: "진취적이다", type: "D" },
            { text: "관대하다", type: "S" },
            { text: "활발하다", type: "I" },
            { text: "정확하다", type: "C" }
        ]
    },
    {
        id: 21,
        options: [
            { text: "행동 지향적이다", type: "D" },
            { text: "참을성이 있다", type: "S" },
            { text: "인기가 있다", type: "I" },
            { text: "세심하다", type: "C" }
        ]
    },
    {
        id: 22,
        options: [
            { text: "솔직하다", type: "D" },
            { text: "좋은 들어주는 사람이다", type: "S" },
            { text: "활기찬", type: "I" },
            { text: "완벽주의자다", type: "C" }
        ]
    },
    {
        id: 23,
        options: [
            { text: "강압적이다", type: "D" },
            { text: "온순하다", type: "S" },
            { text: "즐거운", type: "I" },
            { text: "정확하다", type: "C" }
        ]
    },
    {
        id: 24,
        options: [
            { text: "경쟁적이다", type: "D" },
            { text: "온화하다", type: "S" },
            { text: "인기가 있다", type: "I" },
            { text: "체계적이다", type: "C" }
        ]
    },
    // 추가 질문들 (총 48개까지)
    {
        id: 25,
        options: [
            { text: "성급하다", type: "D" },
            { text: "협조적이다", type: "S" },
            { text: "낙관적이다", type: "I" },
            { text: "꼼꼼하다", type: "C" }
        ]
    },
    {
        id: 26,
        options: [
            { text: "담대하다", type: "D" },
            { text: "수용적이다", type: "S" },
            { text: "매력적이다", type: "I" },
            { text: "신중하다", type: "C" }
        ]
    },
    {
        id: 27,
        options: [
            { text: "자기 확신이 있다", type: "D" },
            { text: "협력적이다", type: "S" },
            { text: "활기찬", type: "I" },
            { text: "신중하다", type: "C" }
        ]
    },
    {
        id: 28,
        options: [
            { text: "독립적이다", type: "D" },
            { text: "의존적이다", type: "S" },
            { text: "영감을 준다", type: "I" },
            { text: "따분하다", type: "C" }
        ]
    },
    {
        id: 29,
        options: [
            { text: "목적 지향적이다", type: "D" },
            { text: "안전 지향적이다", type: "S" },
            { text: "감정적이다", type: "I" },
            { text: "완벽 지향적이다", type: "C" }
        ]
    },
    {
        id: 30,
        options: [
            { text: "빠르다", type: "D" },
            { text: "편안하다", type: "S" },
            { text: "명랑하다", type: "I" },
            { text: "꼼꼼하다", type: "C" }
        ]
    },
    {
        id: 31,
        options: [
            { text: "적극적이다", type: "D" },
            { text: "지지적이다", type: "S" },
            { text: "생동감 있다", type: "I" },
            { text: "분석적이다", type: "C" }
        ]
    },
    {
        id: 32,
        options: [
            { text: "명령적이다", type: "D" },
            { text: "온화하다", type: "S" },
            { text: "표현력이 풍부하다", type: "I" },
            { text: "체계적이다", type: "C" }
        ]
    },
    {
        id: 33,
        options: [
            { text: "추진력이 있다", type: "D" },
            { text: "신뢰할 만하다", type: "S" },
            { text: "표현력이 있다", type: "I" },
            { text: "정확하다", type: "C" }
        ]
    },
    {
        id: 34,
        options: [
            { text: "도전적이다", type: "D" },
            { text: "수용적이다", type: "S" },
            { text: "매력적이다", type: "I" },
            { text: "신중하다", type: "C" }
        ]
    },
    {
        id: 35,
        options: [
            { text: "모험적이다", type: "D" },
            { text: "안정적이다", type: "S" },
            { text: "사회적이다", type: "I" },
            { text: "정확하다", type: "C" }
        ]
    },
    {
        id: 36,
        options: [
            { text: "강력하다", type: "D" },
            { text: "안정된", type: "S" },
            { text: "생동감 있다", type: "I" },
            { text: "완벽하다", type: "C" }
        ]
    },
    {
        id: 37,
        options: [
            { text: "과감하다", type: "D" },
            { text: "친절하다", type: "S" },
            { text: "즐거운", type: "I" },
            { text: "정밀하다", type: "C" }
        ]
    },
    {
        id: 38,
        options: [
            { text: "대담하다", type: "D" },
            { text: "충직하다", type: "S" },
            { text: "즐거운", type: "I" },
            { text: "정확하다", type: "C" }
        ]
    },
    {
        id: 39,
        options: [
            { text: "진취적이다", type: "D" },
            { text: "관대하다", type: "S" },
            { text: "인기가 있다", type: "I" },
            { text: "정밀하다", type: "C" }
        ]
    },
    {
        id: 40,
        options: [
            { text: "강인하다", type: "D" },
            { text: "온화하다", type: "S" },
            { text: "활발하다", type: "I" },
            { text: "완벽주의자다", type: "C" }
        ]
    },
    {
        id: 41,
        options: [
            { text: "자기주장이 강하다", type: "D" },
            { text: "수용적이다", type: "S" },
            { text: "감정 표현이 풍부하다", type: "I" },
            { text: "정확하다", type: "C" }
        ]
    },
    {
        id: 42,
        options: [
            { text: "직접적이다", type: "D" },
            { text: "온화하다", type: "S" },
            { text: "감정적이다", type: "I" },
            { text: "정확하다", type: "C" }
        ]
    },
    {
        id: 43,
        options: [
            { text: "도전을 즐긴다", type: "D" },
            { text: "충성스럽다", type: "S" },
            { text: "재미있다", type: "I" },
            { text: "체계적이다", type: "C" }
        ]
    },
    {
        id: 44,
        options: [
            { text: "행동력이 있다", type: "D" },
            { text: "친근하다", type: "S" },
            { text: "매력적이다", type: "I" },
            { text: "신중하다", type: "C" }
        ]
    },
    {
        id: 45,
        options: [
            { text: "결정력이 있다", type: "D" },
            { text: "지지적이다", type: "S" },
            { text: "영감을 준다", type: "I" },
            { text: "정확하다", type: "C" }
        ]
    },
    {
        id: 46,
        options: [
            { text: "강력한 의지를 가졌다", type: "D" },
            { text: "안정을 추구한다", type: "S" },
            { text: "표현력이 풍부하다", type: "I" },
            { text: "완벽을 추구한다", type: "C" }
        ]
    },
    {
        id: 47,
        options: [
            { text: "자신만만하다", type: "D" },
            { text: "참을성이 있다", type: "S" },
            { text: "활발하다", type: "I" },
            { text: "정밀하다", type: "C" }
        ]
    },
    {
        id: 48,
        options: [
            { text: "진취적이다", type: "D" },
            { text: "안정적이다", type: "S" },
            { text: "사교적이다", type: "I" },
            { text: "완벽주의자다", type: "C" }
        ]
    }
];