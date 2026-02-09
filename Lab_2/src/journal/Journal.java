package journal;

import java.util.ArrayList;
import java.util.HashMap;

public class Journal implements Runnable {
    private final GradeTask gradeTask;

    // group - student list
    private final HashMap<String, HashMap<String, ArrayList<Integer>>> journal;

    public Journal(GradeTask gradeTask, int groupCount, int studentCount) {
        journal = new HashMap<>();
        var dataFactory = new DataFactory(groupCount, studentCount);
        this.gradeTask = gradeTask;

        for (var i = 0; i < groupCount; i++) {
            var group = dataFactory.getGroup(i + 1);
            journal.put(group, new HashMap<>());
            for (var j = 0; j < studentCount; j++) {
                var name = dataFactory.getName(j + 1);
                journal.get(group).put(name, new ArrayList<>());
            }
        }
    }

    public synchronized void addGrade(String group, String name, int grade) {
        journal.get(group).get(name).add(grade);
    }

    public synchronized String[] getGroups() {
        return journal.keySet().toArray(new String[0]);
    }

    public synchronized String[] getStudents(String group) {
        return journal.get(group).keySet().toArray(new String[0]);
    }

    public synchronized ArrayList<Integer> getGrades(String group, String student) {
        return journal.get(group).get(student);
    }

    public synchronized void printJournal() {
        for (var group : getGroups()) {
            System.out.println(group);
            for (var student : getStudents(group)) {
                System.out.printf("%-20s |", student);
                for (var grade : getGrades(group, student)) {
                    System.out.printf(" %3d |", grade);
                }
            }
        }
    }

    public void run() {
        for (var grade = gradeTask.take(); grade != null; grade = gradeTask.take()) {
            var teacher = grade.teacher();
            var group = grade.group();
            var name = grade.name();
            var newGrade = grade.grade();
            addGrade(group, name, newGrade);
            System.out.format("%-20s %-5s %-10s %-3d\n", teacher, group, name, newGrade);
        }
    }
}
