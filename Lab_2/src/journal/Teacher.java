package journal;

import java.util.ArrayList;

public class Teacher extends BaseTeacher {
    public Teacher(GradeTask gradeTask, ArrayList<GradeItem> grades, String name) {
        super(gradeTask, grades, name);
    }

    @Override
    protected String getPosition() {
        return "Вчитель";
    }
}

