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

        var threads = new Thread[] {
            new Thread(teacher),
            new Thread(assistant1),
            new Thread(assistant2),
            new Thread(assistant3),
            new Thread(journal)
        };

        for (var t : threads) {
            t.start();
        }

        try {
            for (var t : threads) {
                t.join();
            }
        }
        catch (InterruptedException _) {}
    }
}