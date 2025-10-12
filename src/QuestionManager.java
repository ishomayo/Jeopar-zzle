import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class QuestionManager {
    private Map<Integer, List<Question>> questionsByModule; // Module number -> Questions
    private Map<String, List<Question>> questionsByCategory;
    private Random random;

    // Mapping: point value -> module number
    private static final Map<Integer, Integer> POINT_TO_MODULE = new HashMap<Integer, Integer>() {
        {
            put(100, 1);
            put(200, 2);
            put(400, 3);
            put(600, 4);
            put(800, 5);
            put(1000, 6);
            put(1200, 7);
            put(1500, 8);
        }
    };

    private String[] categories = { "KNOWLEDGE", "COMPREHENSION", "APPLICATION", "ANALYSIS", "SYNTHESIS",
            "EVALUATION" };

    public static class Question {
        private String questionText;
        private String[] answers;
        private String correctAnswer;
        private int pointValue;
        private String category;
        private int moduleNumber;

        public Question(String questionText, String[] answers, String correctAnswer, int pointValue, String category,
                int moduleNumber) {
            this.questionText = questionText;
            this.answers = answers;
            this.correctAnswer = correctAnswer;
            this.pointValue = pointValue;
            this.category = category;
            this.moduleNumber = moduleNumber;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String[] getAnswers() {
            return answers;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public int getPointValue() {
            return pointValue;
        }

        public String getCategory() {
            return category;
        }

        public int getModuleNumber() {
            return moduleNumber;
        }

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
                    ", module=" + moduleNumber +
                    ", correctAnswer='" + correctAnswer + '\'' +
                    '}';
        }
    }

    public QuestionManager() {
        this.random = new Random();
        this.questionsByModule = new HashMap<>();
        this.questionsByCategory = new HashMap<>();
        loadAllModules();
    }

    private void loadAllModules() {
        System.out.println("=== LOADING MODULES ===");

        // Load each module (1-8)
        for (int module = 1; module <= 8; module++) {
            String filename = "/mod" + module + ".csv";
            int pointValue = getPointValueForModule(module);

            System.out.println(
                    "\nAttempting to load: " + filename + " (Module " + module + ", " + pointValue + " points)");

            InputStream csvStream = getClass().getResourceAsStream(filename);

            if (csvStream != null) {
                try {
                    parseCSVFromStream(csvStream, pointValue, module);
                    System.out.println("✓ Successfully loaded " + filename);
                } catch (IOException e) {
                    System.err.println("✗ Error parsing " + filename + ": " + e.getMessage());
                    createFallbackQuestionsForModule(module, pointValue);
                }
            } else {
                System.out.println("✗ File not found: " + filename + " - Creating fallback questions");
                createFallbackQuestionsForModule(module, pointValue);
            }
        }

        System.out.println("\n=== LOADING COMPLETE ===");
        System.out.println("Total questions loaded: " + getTotalQuestions());
        System.out.println("Modules loaded: " + questionsByModule.keySet());
        System.out.println("Categories: " + questionsByCategory.keySet());
    }

    private int getPointValueForModule(int module) {
        for (Map.Entry<Integer, Integer> entry : POINT_TO_MODULE.entrySet()) {
            if (entry.getValue() == module) {
                return entry.getKey();
            }
        }
        return 100; // Default fallback
    }

    private void parseCSVFromStream(InputStream csvStream, int pointValue, int moduleNumber) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(csvStream, StandardCharsets.UTF_8));
        String line;
        int lineNumber = 0;
        int questionsAdded = 0;

        System.out.println("  Parsing Module " + moduleNumber + " (comma-delimited)...");

        // Skip header line
        reader.readLine();

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (line.trim().isEmpty())
                continue;

            try {
                // Handle quoted commas safely
                String[] fields = parseCSVLine(line);
                if (fields.length < 7) {
                    System.err.println(
                            "  Skipping incomplete line " + lineNumber + ": only " + fields.length + " fields");
                    continue;
                }

                String category = cleanText(fields[0]);
                String questionText = cleanText(fields[1]);
                String[] answers = {
                        cleanText(fields[2]),
                        cleanText(fields[3]),
                        cleanText(fields[4]),
                        cleanText(fields[5])
                };
                String correctAnswer = cleanText(fields[6]);

                if (questionText.isEmpty() || correctAnswer.isEmpty())
                    continue;

                Question question = new Question(
                        questionText, answers, correctAnswer, pointValue, category, moduleNumber);

                questionsByModule.computeIfAbsent(moduleNumber, k -> new ArrayList<>()).add(question);
                questionsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(question);
                questionsAdded++;

                if (questionsAdded <= 3) {
                    System.out.println("  ✓ Added Q" + questionsAdded + " (" + category + "): " +
                            questionText.substring(0, Math.min(60, questionText.length())) + "...");
                }

            } catch (Exception e) {
                System.err.println("  ✗ Error parsing line " + lineNumber + ": " + e.getMessage());
            }
        }

        reader.close();

        System.out.println("  ✓ Finished parsing Module " + moduleNumber + " - Added " + questionsAdded + " questions");

        if (questionsAdded == 0) {
            System.err.println("  ✗ WARNING: No valid questions parsed for Module " + moduleNumber);
            createFallbackQuestionsForModule(moduleNumber, pointValue);
        }
    }

    private void createFallbackQuestionsForModule(int moduleNumber, int pointValue) {
        System.out.println("Creating fallback questions for Module " + moduleNumber);

        List<Question> fallbackQuestions = new ArrayList<>();

        // Create 6 questions (one per category)
        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];

            String questionText = "Lorem ipsum dolor sit amet for Module " + moduleNumber +
                    " (" + category + " - " + pointValue + " points)?";

            String[] answers = {
                    "Lorem ipsum answer A",
                    "Lorem ipsum answer B",
                    "Lorem ipsum answer C",
                    "Lorem ipsum answer D"
            };

            String correctAnswer = answers[0]; // First answer is correct

            Question question = new Question(questionText, answers, correctAnswer, pointValue, category, moduleNumber);
            fallbackQuestions.add(question);

            // Add to category mapping
            questionsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(question);
        }

        questionsByModule.put(moduleNumber, fallbackQuestions);
        System.out.println("✓ Created " + fallbackQuestions.size() + " fallback questions for Module " + moduleNumber);
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
        // Determine which module to use based on points
        Integer moduleNumber = POINT_TO_MODULE.get(points);

        if (moduleNumber == null) {
            System.out.println("WARNING: No module mapping for points " + points);
            return getRandomQuestion();
        }

        System.out.println(
                "Looking for question: Category=" + category + ", Points=" + points + ", Module=" + moduleNumber);

        // Get questions from the specific module
        List<Question> moduleQuestions = questionsByModule.get(moduleNumber);

        if (moduleQuestions != null && !moduleQuestions.isEmpty()) {
            // Filter by category within the module
            List<Question> matchingQuestions = new ArrayList<>();
            for (Question q : moduleQuestions) {
                if (q.getCategory().equals(category)) {
                    matchingQuestions.add(q);
                }
            }

            if (!matchingQuestions.isEmpty()) {
                Question selected = matchingQuestions.get(random.nextInt(matchingQuestions.size()));
                System.out.println("✓ Found question from Module " + moduleNumber + ": " +
                        selected.getQuestionText().substring(0, Math.min(60, selected.getQuestionText().length())));
                return selected;
            }

            // If no category match, return random from module
            System.out.println("No category match, returning random from Module " + moduleNumber);
            return moduleQuestions.get(random.nextInt(moduleQuestions.size()));
        }

        // Ultimate fallback
        System.out.println("Module not found, using random question");
        return getRandomQuestion();
    }

    public Question getRandomQuestion() {
        List<Question> allQuestions = new ArrayList<>();
        for (List<Question> questions : questionsByModule.values()) {
            allQuestions.addAll(questions);
        }

        if (allQuestions.isEmpty()) {
            return null;
        }
        return allQuestions.get(random.nextInt(allQuestions.size()));
    }

    public Set<String> getCategories() {
        return questionsByCategory.keySet();
    }

    public int getTotalQuestions() {
        int total = 0;
        for (List<Question> questions : questionsByModule.values()) {
            total += questions.size();
        }
        return total;
    }

    public List<Question> getQuestionsByCategory(String category) {
        return questionsByCategory.getOrDefault(category, new ArrayList<>());
    }

    public List<Question> getQuestionsByModule(int moduleNumber) {
        return questionsByModule.getOrDefault(moduleNumber, new ArrayList<>());
    }

    public Question getShuffledQuestion(Question original) {
        if (original == null)
            return null;

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
                original.getCategory(),
                original.getModuleNumber());
    }

    public void printAllQuestions() {
        System.out.println("=== ALL QUESTIONS BY MODULE ===");
        for (int module = 1; module <= 8; module++) {
            List<Question> questions = questionsByModule.get(module);
            if (questions != null) {
                System.out.println("\n--- Module " + module + " (" + questions.size() + " questions) ---");
                for (int i = 0; i < questions.size(); i++) {
                    System.out.println((i + 1) + ". " + questions.get(i).toString());
                }
            }
        }
    }
}