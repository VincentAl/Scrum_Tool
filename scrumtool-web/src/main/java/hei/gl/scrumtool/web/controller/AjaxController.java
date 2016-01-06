package hei.gl.scrumtool.web.controller;

import hei.gl.scrumtool.core.dto.TaskDTO; 
import java.util.List;
import hei.gl.scrumtool.core.entity.Story;
import hei.gl.scrumtool.core.entity.Task;
import hei.gl.scrumtool.core.entity.View;
import hei.gl.scrumtool.core.entity.User;
import hei.gl.scrumtool.core.enumeration.StoryColumn;
import hei.gl.scrumtool.core.enumeration.TaskColumn;
import hei.gl.scrumtool.core.service.StoryService;
import hei.gl.scrumtool.core.service.TaskService;
import hei.gl.scrumtool.core.service.UserService;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;

@Controller
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AjaxController {
	private final static Logger logger = LoggerFactory.getLogger(AjaxController.class);

	@Inject
	private StoryService storyService;
	
	@Inject
	private TaskService taskService;
	
	@Inject
	private UserService userService;

	@ResponseBody
	@RequestMapping(value = "/story/{id}", method = RequestMethod.DELETE)
	public String deleteOneStory(@PathVariable("id") long id) {
		storyService.delete(id);
		return "{}";
	}
	
	@ResponseBody
	@RequestMapping(value = "/task/{id}", method = RequestMethod.DELETE)
	public String deleteOneTask(@PathVariable("id") long id) {
		taskService.delete(id);
		return "{}";}
	

	@ResponseBody
	@RequestMapping(value = "/story/{id}", method = RequestMethod.PATCH)
	public String updateStory(@PathVariable("id") long id ,@RequestBody Story story) {
		Story oldStory = storyService.findById(id);
		// pour ne pas écraser les anciennes valeurs à null
		story.setCategory(oldStory.getCategory());
		story.setPriority(oldStory.getPriority());
		story.setSprint(oldStory.getSprint());
		story.setTasksList(oldStory.getTasksList());
		// update la story avec les nouvelles valeurs
		storyService.update(story);
		return "{}";
	}

	@ResponseBody
	@JsonView(View.Summary.class)
	@RequestMapping(value = "/story/{id}", method = RequestMethod.GET)
	public Story getOneStory(@PathVariable("id") long id) {
		return storyService.findById(id);
	}

	@ResponseBody
	@RequestMapping(value = "/story/{id}/column/{id_col}/previous/{id_previous}", method = RequestMethod.PUT)
	public String updateOneStory(@PathVariable("id") long id, @PathVariable("id_col") long idCol, @PathVariable("id_previous") long idPrev) {
		
		if(idPrev == -1){
			storyService.move(id, StoryColumn.getStoryColumnById(idCol));
		}else{
			storyService.move(id, StoryColumn.getStoryColumnById(idCol), 
					storyService.findById(idPrev).getPriority());
		}
		return "{}";
	}
	
	@ResponseBody
	@RequestMapping(value = "/task/{id}/column/{id_col}/previous/{id_previous}", method = RequestMethod.PUT)
	public String updateOneTask(@PathVariable("id") long id, @PathVariable("id_col") long idCol, @PathVariable("id_previous") long idPrev) {

		logger.debug("Canard - "+idCol+" - "+id);
		if(idPrev == -1){
			taskService.moveTask(taskService.findByID(id), TaskColumn.getTaskColumnById(idCol));
		}else{
			taskService.moveTask(taskService.findByID(id), TaskColumn.getTaskColumnById(idCol), taskService.findByID(idPrev).getPriority());
		}
		return "{}";
	}
	
	@ResponseBody
	@JsonView(View.Summary.class)
	@RequestMapping(value="/new-task", method=RequestMethod.POST)
	public Task addNewTask(@RequestBody TaskDTO taskDTO){
		Task newTask = new Task(taskDTO.getTaskTitle(), taskDTO.getTaskDescription(), TaskColumn.TODO);
		taskService.save(newTask);
		newTask.setStory(storyService.findById(taskDTO.getIdStory()));
		newTask.setUser(userService.findByID(taskDTO.getIdUser()));
		newTask.setPriority(taskService.findByCategory(newTask.getCategory()).size());
		newTask = taskService.save(newTask);	
		return newTask;
	}
	
	@ResponseBody
	@JsonView(View.Summary.class)
	@RequestMapping(value = "/task/{idStory}", method = RequestMethod.GET)
	public List<Task> getTasksByStory(@PathVariable("idStory") long idStory){
		
		return taskService.findByStory(storyService.findById(idStory));
	}

}