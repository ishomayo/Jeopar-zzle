import java.io.*;
import java.util.*;

public class QuestionManager {
    private List<Question> allQuestions;
    private Map<String, List<Question>> questionsByCategory;
    private Random random;
    
    public static class Question {
        private String questionText;
        private String[] answers;
        private String correctAnswer;
        private int pointValue;
        private String category;
        
        public Question(String questionText, String[] answers, String correctAnswer, int pointValue, String category) {
            this.questionText = questionText;
            this.answers = answers;
            this.correctAnswer = correctAnswer;
            this.pointValue = pointValue;
            this.category = category;
        }
        
        public String getQuestionText() { return questionText; }
        public String[] getAnswers() { return answers; }
        public String getCorrectAnswer() { return correctAnswer; }
        public int getPointValue() { return pointValue; }
        public String getCategory() { return category; }
        
        public int getCorrectAnswerIndex() {
            for (int i = 0; i < answers.length; i++) {
                if (answers[i] != null && answers[i].equals(correctAnswer)) {
                    return i;
                }
            }
            return 0; 
        }
        
        @Override
        public String toString() {
            return "Question{" +
                    "text='" + questionText + '\'' +
                    ", pointValue=" + pointValue +
                    ", category='" + category + '\'' +
                    ", correctAnswer='" + correctAnswer + '\'' +
                    '}';
        }
    }
    
    public QuestionManager() {
        this.random = new Random();
        this.allQuestions = new ArrayList<>();
        this.questionsByCategory = new HashMap<>();
        loadQuestionsFromCSV();
    }
    
    private void loadQuestionsFromCSV() {
        try {
            InputStream csvStream = getClass().getResourceAsStream("/Q&A Pairs.csv");
            if (csvStream == null) {
                csvStream = getClass().getClassLoader().getResourceAsStream("Q&A Pairs.csv");
            }
            
            if (csvStream != null) {
                System.out.println("Loading questions from CSV resource...");
                parseCSVFromStream(csvStream);
            } else {
                
            }
            
        } catch (Exception e) {
            System.err.println("Error loading CSV: " + e.getMessage());
            e.printStackTrace();
            
        }
        
        System.out.println("Total questions loaded: " + allQuestions.size());
        System.out.println("Categories: " + questionsByCategory.keySet());
    }
    
    private void parseCSVFromStream(InputStream csvStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream));
        String line;
        boolean isFirstLine = true;
        int lineNumber = 0;
        
        String[] categories = {"KNOWLEDGE", "COMPREHENSION", "APPLICATION", "ANALYSIS", "SYNTHESIS", "EVALUATION"};
        int[] pointValues = {100, 400, 800, 1200, 1500};
        
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            
            // Skip header line
            if (isFirstLine) {
                isFirstLine = false;
                continue;
            }
            
            try {
                // Parse CSV line manually to handle quotes properly
                String[] fields = parseCSVLine(line);
                
                if (fields.length >= 6) {
                    String questionText = cleanText(fields[0]);
                    
                    // Extract the four possible answers
                    String[] answers = new String[4];
                    answers[0] = cleanText(fields[1]);
                    answers[1] = cleanText(fields[2]);
                    answers[2] = cleanText(fields[3]);
                    answers[3] = cleanText(fields[4]);
                    
                    String correctAnswer = cleanText(fields[5]);
                    
                    // Skip if any essential field is empty
                    if (questionText.isEmpty() || correctAnswer.isEmpty()) {
                        continue;
                    }
                    
                    // Determine category and point value based on question index
                    int questionIndex = lineNumber - 2; // Adjust for header and 0-based index
                    String category = categories[questionIndex % categories.length];
                    int pointValue = pointValues[(questionIndex / categories.length) % pointValues.length];
                    
                    // Create question object
                    Question question = new Question(questionText, answers, correctAnswer, pointValue, category);
                    allQuestions.add(question);
                    
                    // Add to category mapping
                    questionsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(question);
                    
                    System.out.println("Loaded Q" + lineNumber + ": " + questionText.substring(0, Math.min(50, questionText.length())) + "...");
                }
                
            } catch (Exception e) {
                System.err.println("Error parsing line " + lineNumber + ": " + e.getMessage());
                System.err.println("Line content: " + line);
            }
        }
        
        reader.close();
    }
    
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString());
        
        return fields.toArray(new String[0]);
    }
    
    private String cleanText(String text) {
        if (text == null) {
            return "";
        }
        
        return text.trim()
                   .replaceAll("^\"+|\"+$", "")  
                   .replace("\"\"", "\"")      
                   .trim();
    }
    
  


    public Question getQuestionForCategoryAndPoints(String category, int points) {
        List<Question> categoryQuestions = questionsByCategory.get(category);
        if (categoryQuestions != null && !categoryQuestions.isEmpty()) {
            List<Question> matchingQuestions = new ArrayList<>();
            for (Question q : categoryQuestions) {
                if (q.getPointValue() == points) {
                    matchingQuestions.add(q);
                }
            }
            
            if (!matchingQuestions.isEmpty()) {
                return matchingQuestions.get(random.nextInt(matchingQuestions.size()));
            }
        }
        
        for (Question q : allQuestions) {
            if (q.getPointValue() == points) {
                return q;
            }
        }

        return allQuestions.isEmpty() ? null : allQuestions.get(0);
    }
    
    public Question getRandomQuestion() {
        if (allQuestions.isEmpty()) {
            return null;
        }
        return allQuestions.get(random.nextInt(allQuestions.size()));
    }
   
    public Set<String> getCategories() {
        return questionsByCategory.keySet();
    }
    
  
    public int getTotalQuestions() {
        return allQuestions.size();
    }
    
    public List<Question> getQuestionsByCategory(String category) {
        return questionsByCategory.getOrDefault(category, new ArrayList<>());
    }
    
    public Question getShuffledQuestion(Question original) {
        if (original == null) return null;
        
        String[] shuffledAnswers = original.getAnswers().clone();
        String correctAnswer = original.getCorrectAnswer();
        
        List<String> answerList = new ArrayList<>();
        for (String answer : shuffledAnswers) {
            if (answer != null && !answer.trim().isEmpty()) {
                answerList.add(answer);
            }
        }
        
        Collections.shuffle(answerList);
        
        String[] result = new String[4];
        for (int i = 0; i < result.length; i++) {
            result[i] = i < answerList.size() ? answerList.get(i) : null;
        }
        
        return new Question(
            original.getQuestionText(),
            result,
            correctAnswer,
            original.getPointValue(),
            original.getCategory()
        );
    }
    
    public void printAllQuestions() {
        System.out.println("=== ALL QUESTIONS ===");
        for (int i = 0; i < allQuestions.size(); i++) {
            Question q = allQuestions.get(i);
            System.out.println((i + 1) + ". " + q.toString());
        }
    }
}