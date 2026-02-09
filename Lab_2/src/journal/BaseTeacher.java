package journal;

import java.util.ArrayList;

public abstract class BaseTeacher implements Runnable {
    protected GradeTask gradeTask;
    protected ArrayList<GradeItem> grades;
    protected String name;
    protected abstract String getPosition();

    public BaseTeacher(GradeTask gradeTask, ArrayList<GradeItem> grades, String name) {
        this.gradeTask = gradeTask;
        this.grades = grades;
        this.name = name;
    }

    public void run() {
        for (var grade : grades) {
            var operation = new GradeOperation(
                    String.format("%s %s", getPosition(), name),
                    grade.group(),
                    grade.name(),
                    grade.grade());
            gradeTask.put(operation);
        }
    }
}
