import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class Course {
    private String courseName;
    private String duration;
    public Course(String courseName, String duration) {
        this.courseName = courseName;
        this.duration = duration;
    }
    public String getCourseName() {
        return courseName;
    }
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
    public String getDuration() {
        return duration;
    }
    public void setDuration(String duration) {
        this.duration = duration;
    }
    @Override
    public String toString() {
        return "Course [courseName=" + courseName + ", duration=" + duration + "]";
    }
}
class Student {
    private String name;
    private Course course;
    public Student(String name, Course course) {
        this.name = name;
        this.course = course;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Course getCourse() {
        return course;
    }
    public void setCourse(Course course) {
        this.course = course;
    }
    @Override
    public String toString() {
        return "Student [name=" + name + ", course=" + course + "]";
    }
}
@Configuration
class AppConfig {
    @Bean
    public Course course() {
        return new Course("Java Programming", "3 Months");
    }
    @Bean
    public Student student() {
        return new Student("John Doe", course()); 
    }
}
public class Exp9 {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Student student = context.getBean(Student.class);
        System.out.println(student);
        context.close();
    }
}
