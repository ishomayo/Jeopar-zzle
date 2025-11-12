
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

    private String[] categories = {"KNOWLEDGE", "COMPREHENSION", "APPLICATION", "ANALYSIS", "SYNTHESIS",
        "EVALUATION"};

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
            return -1;
        }

        @Override
        public String toString() {
            return "Question{"
                    + "text='" + questionText + '\''
                    + ", pointValue=" + pointValue
                    + ", category='" + category + '\''
                    + ", module=" + moduleNumber
                    + ", correctAnswer='" + correctAnswer + '\''
                    + '}';
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

                }
            } else {

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
            if (line.trim().isEmpty()) {
                continue;
            }

            try {
                // Handle quoted commas safely
                String[] fields = parseCSVLine(line);
                if (fields.length < 7) {
                    System.err.println(
                            "  Skipping incomplete line " + lineNumber + ": only " + fields.length + " fields");
                    continue;
                }

                String category = cleanText(fields[0]).trim();
                String questionText = cleanText(fields[1]).trim();
                String[] answers = {
                    cleanText(fields[2]).trim(),
                    cleanText(fields[3]).trim(),
                    cleanText(fields[4]).trim(),
                    cleanText(fields[5]).trim()
                };
                String correctAnswer = cleanText(fields[6]).trim();

                if (questionText.isEmpty() || correctAnswer.isEmpty()) {
                    continue;
                }

                // DEBUG: check if correctAnswer matches one of the answers
                boolean matchFound = false;
                for (String ans : answers) {
                    if (ans.equals(correctAnswer)) {
                        matchFound = true;
                        break;
                    }
                }

                if (!matchFound) {
                    System.err.println("  WARNING: Correct answer does not match options (line " + lineNumber + ")");
                    System.err.println("    Answers: " + Arrays.toString(answers));
                    System.err.println("    Correct: '" + correctAnswer + "'");
                }

                Question question = new Question(
                        questionText, answers, correctAnswer, pointValue, category, moduleNumber);

                questionsByModule.computeIfAbsent(moduleNumber, k -> new ArrayList<>()).add(question);
                questionsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(question);
                questionsAdded++;

                if (questionsAdded <= 3) {
                    System.out.println("  ✓ Added Q" + questionsAdded + " (" + category + "): "
                            + questionText.substring(0, Math.min(60, questionText.length())) + "...");
                }

            } catch (Exception e) {
                System.err.println("  ✗ Error parsing line " + lineNumber + ": " + e.getMessage());
            }
        }

        reader.close();
        System.out.println("  ✓ Finished parsing Module " + moduleNumber + " - Added " + questionsAdded + " questions");
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
            return getShuffledQuestion(getRandomQuestion()); // shuffle fallback
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

            Question selected;
            if (!matchingQuestions.isEmpty()) {
                selected = matchingQuestions.get(random.nextInt(matchingQuestions.size()));
                System.out.println("✓ Found question from Module " + moduleNumber + ": "
                        + selected.getQuestionText().substring(0, Math.min(60, selected.getQuestionText().length())));
            } else {
                // If no category match, return random from module
                System.out.println("No category match, returning random from Module " + moduleNumber);
                selected = moduleQuestions.get(random.nextInt(moduleQuestions.size()));
            }

            // Return the shuffled version of the selected question
            return getShuffledQuestion(selected);
        }

        // Ultimate fallback
        System.out.println("Module not found, using random question");
        return getShuffledQuestion(getRandomQuestion());
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
        if (original == null) {
            return null;
        }

        // Copy the answers into a list for easy shuffling
        List<String> answerList = new ArrayList<>();
        for (String ans : original.getAnswers()) {
            if (ans != null && !ans.trim().isEmpty()) {
                answerList.add(ans);
            }
        }

        // Shuffle the answers
        Collections.shuffle(answerList);

        // Put them back into an array of size 4
        String[] shuffledAnswers = new String[4];
        for (int i = 0; i < shuffledAnswers.length; i++) {
            shuffledAnswers[i] = i < answerList.size() ? answerList.get(i) : null;
        }

        // Determine the new correct index
        int newCorrectIndex = -1;
        for (int i = 0; i < shuffledAnswers.length; i++) {
            if (shuffledAnswers[i].equals(original.getCorrectAnswer())) {
                newCorrectIndex = i;
                break;
            }
        }

        // Return a new Question object with shuffled answers
        return new Question(
                original.getQuestionText(),
                shuffledAnswers,
                original.getCorrectAnswer(), // correct answer string stays the same
                original.getPointValue(),
                original.getCategory(),
                original.getModuleNumber()
        );
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
