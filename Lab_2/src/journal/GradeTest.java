package journal;

import journal.*;

class GradeTest {
    void main() {
        int groupCount = 5;
        int studentCount = 25;

        var gradeTask = new GradeTask();
        var dataFactory = new DataFactory(groupCount, studentCount);

        var journal = new Journal(gradeTask, groupCount, studentCount);

        var teacher = new Teacher(
                gradeTask,
                dataFactory.getGradeList(250),
                "Ім'я 1"
        );
        var assistant1 = new Assistant(
                gradeTask,
                dataFactory.getGradeList(50),
                "Ім'я 2"
        );
        var assistant2 = new Assistant(
                gradeTask,
                dataFactory.getGradeList(50),
                "Ім`я 3"
        );
        var assistant3 = new Assistant(
                gradeTask,
                dataFactory.getGradeList(50),
                "Ім`я 4"
        );

        new Thread(teacher).start();
        new Thread(assistant1).start();
        new Thread(assistant2).start();
        new Thread(assistant3).start();
        new Thread(journal).start();
    }
}