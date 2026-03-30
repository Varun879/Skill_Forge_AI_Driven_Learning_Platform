package com.skillforge.domain.course.dto;

public class CourseModuleProgressResponse {

    private Long courseId;
    private long completedModules;
    private long totalModules;
    private boolean allModulesCompleted;

    public CourseModuleProgressResponse(Long courseId, long completedModules, long totalModules) {
        this.courseId = courseId;
        this.completedModules = completedModules;
        this.totalModules = totalModules;
        this.allModulesCompleted = totalModules > 0 && completedModules >= totalModules;
    }

    public Long getCourseId() { return courseId; }
    public long getCompletedModules() { return completedModules; }
    public long getTotalModules() { return totalModules; }
    public boolean isAllModulesCompleted() { return allModulesCompleted; }
}
