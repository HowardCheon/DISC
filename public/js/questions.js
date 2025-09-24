// DISC 질문 데이터
const DISC_QUESTIONS = [
    {
        id: 1,
        options: [
            { text: "명령적인", type: "D" },
            { text: "대담한", type: "D" },
            { text: "지휘적", type: "D" },
            { text: "만족해 하는", type: "S" }
        ]
    },
    {
        id: 2,
        options: [
            { text: "신중한", type: "C" },
            { text: "결단력 있는", type: "D" },
            { text: "활실을 주는", type: "I" },
            { text: "온의적인", type: "S" }
        ]
    },
    {
        id: 3,
        options: [
            { text: "다정한", type: "S" },
            { text: "정확한", type: "C" },
            { text: "충실하게 맡하는", type: "S" },
            { text: "변화가 적은", type: "C" }
        ]
    },
    {
        id: 4,
        options: [
            { text: "말하기 좋아하는", type: "I" },
            { text: "자제력 있는", type: "C" },
            { text: "관습을 따르는", type: "S" },
            { text: "결단력 있는", type: "D" }
        ]
    },
    {
        id: 5,
        options: [
            { text: "도전하는", type: "D" },
            { text: "통찰력 있는", type: "C" },
            { text: "사교적인", type: "I" },
            { text: "온건한", type: "S" }
        ]
    },
    {
        id: 6,
        options: [
            { text: "온화한", type: "S" },
            { text: "설득력 있는", type: "I" },
            { text: "검소한", type: "C" },
            { text: "독창적 아이디어 내는", type: "D" }
        ]
    },
    {
        id: 7,
        options: [
            { text: "표현력 있는", type: "I" },
            { text: "조심성 있는", type: "C" },
            { text: "주도적인", type: "D" },
            { text: "만족한 반응하는", type: "S" }
        ]
    },
    {
        id: 8,
        options: [
            { text: "호의적인", type: "S" },
            { text: "세심한", type: "C" },
            { text: "점순한", type: "S" },
            { text: "착을설이 적은", type: "C" }
        ]
    },
    {
        id: 9,
        options: [
            { text: "사려깊은", type: "C" },
            { text: "낱 의견에 잘 동의하는", type: "S" },
            { text: "매력적인", type: "I" },
            { text: "확고한", type: "D" }
        ]
    },
    {
        id: 10,
        options: [
            { text: "용감한", type: "D" },
            { text: "격려 하는", type: "I" },
            { text: "조용 하는", type: "S" },
            { text: "주좁이 하는", type: "C" }
        ]
    },
    {
        id: 11,
        options: [
            { text: "내성적인", type: "C" },
            { text: "호의적인", type: "S" },
            { text: "의지가 강한", type: "D" },
            { text: "명랑한", type: "I" }
        ]
    },
    {
        id: 12,
        options: [
            { text: "날을 격려하는", type: "I" },
            { text: "신중한", type: "C" },
            { text: "주의깊은", type: "C" },
            { text: "독립심 강한", type: "D" }
        ]
    },
    {
        id: 13,
        options: [
            { text: "경쟁심 있는", type: "D" },
            { text: "새전이 깊은", type: "S" },
            { text: "털직한", type: "I" },
            { text: "자신을 잘 드러내지 않은", type: "C" }
        ]
    },
    {
        id: 14,
        options: [
            { text: "세밀한", type: "C" },
            { text: "유쾌한", type: "I" },
            { text: "완고한", type: "C" },
            { text: "놓기 좋아하는", type: "I" }
        ]
    },
    {
        id: 15,
        options: [
            { text: "사람에게 도왐주는", type: "S" },
            { text: "생각이 깊은", type: "C" },
            { text: "의지가 깊은", type: "D" },
            { text: "일관되게 행동하는", type: "S" }
        ]
    },
    {
        id: 16,
        options: [
            { text: "논리적인", type: "C" },
            { text: "과감한", type: "D" },
            { text: "충실한", type: "S" },
            { text: "인기있는", type: "I" }
        ]
    },
    {
        id: 17,
        options: [
            { text: "사교적인", type: "I" },
            { text: "침을성 있는", type: "S" },
            { text: "자신감 있는", type: "D" },
            { text: "말씨가 부드러운", type: "S" }
        ]
    },
    {
        id: 18,
        options: [
            { text: "의존적인", type: "S" },
            { text: "의욕적인", type: "D" },
            { text: "침전한", type: "C" },
            { text: "활기 있는", type: "I" }
        ]
    },
    {
        id: 19,
        options: [
            { text: "의욕적인", type: "D" },
            { text: "인내적인", type: "S" },
            { text: "친근한", type: "I" },
            { text: "깊은을 퍼하는", type: "C" }
        ]
    },
    {
        id: 20,
        options: [
            { text: "우미가 있는", type: "I" },
            { text: "이해성 있는", type: "S" },
            { text: "공평한", type: "C" },
            { text: "단호한", type: "D" }
        ]
    },
    {
        id: 21,
        options: [
            { text: "자체력 있는", type: "D" },
            { text: "관대한", type: "S" },
            { text: "활기 있는", type: "I" },
            { text: "고집스러운", type: "C" }
        ]
    },
    {
        id: 22,
        options: [
            { text: "제치있는", type: "I" },
            { text: "내향적인", type: "C" },
            { text: "강인한", type: "D" },
            { text: "침게넘게 없는", type: "S" }
        ]
    },
    {
        id: 23,
        options: [
            { text: "남과 잘 어울리는", type: "I" },
            { text: "침착한", type: "C" },
            { text: "활기찬", type: "I" },
            { text: "나누려운", type: "D" }
        ]
    },
    {
        id: 24,
        options: [
            { text: "매혹하는", type: "I" },
            { text: "음측해 하는", type: "C" },
            { text: "지시하는", type: "D" },
            { text: "양보하는", type: "S" }
        ]
    },
    {
        id: 25,
        options: [
            { text: "자기 주장을 하는", type: "D" },
            { text: "세심적인", type: "C" },
            { text: "협력적인", type: "S" },
            { text: "즐거운", type: "I" }
        ]
    },
    {
        id: 26,
        options: [
            { text: "규제한", type: "C" },
            { text: "정교한", type: "C" },
            { text: "결과를 요구하는", type: "D" },
            { text: "침착한", type: "S" }
        ]
    },
    {
        id: 27,
        options: [
            { text: "변화를 추구하는", type: "D" },
            { text: "우호적인", type: "S" },
            { text: "호소적 있는", type: "I" },
            { text: "꿈물한", type: "C" }
        ]
    },
    {
        id: 28,
        options: [
            { text: "음소창", type: "C" },
            { text: "새로게 시작하는", type: "D" },
            { text: "낙천적인", type: "I" },
            { text: "도음을 주려하는", type: "S" }
        ]
    }
];