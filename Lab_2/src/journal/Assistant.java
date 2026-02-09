package journal;

import java.util.ArrayList;

public class Assistant extends BaseTeacher {
    public Assistant(GradeTask gradeTask, ArrayList<GradeItem> grades, String name) {
        super(gradeTask, grades, name);
    }

    @Override
    protected String getPosition() {
        return "Асистент";
    }
}
