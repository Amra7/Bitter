package controllers;

import helpers.*;
import play.data.Form;
import play.mvc.*;
import views.html.user.*;

import models.*;

public class UserController extends Controller {

	/**
	 * 
	 * @return
	 */
	@Security.Authenticated(AdminFilter.class)
	public static Result index() {
		return ok(index.render(User.all()));
	}

	/**
	 * 
	 * @return
	 */
	public static Result newUser() {
		return ok(newUser.render(new Form<User>(User.class)));
	}

	/**
	 * 
	 * @return
	 */
	public static Result create() {
		Form<User> submit = Form.form(User.class).bindFromRequest();
		if (submit.hasErrors() || submit.hasGlobalErrors()) {
			return ok(newUser.render(submit));
		}
		User u = submit.get();
		if (!User.create(u)) {
			return ok(newUser.render(submit));
		}
		SessionController.loginUser(u.username);
		return redirect("/@" + u.username);
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	public static Result show(String username) {
		User u = User.find(username);
		if (u == null)
			return notFound(views.html.static_pages.notFound
					.render("User does not exist"));
		else
			return ok(show.render(u, new Form<Post>(Post.class)));
	}

	/**
	 * 
	 * @param username
	 * @return
	 */
	@Security.Authenticated(CurrentUserFilter.class)
	public static Result edit(String username) {
		User currentUser = SessionHelper.currentUser(ctx());
		if (!currentUser.username.equals(username)) {
			return badRequest(views.html.static_pages.loginToComplete
					.render("You can't edit this profile"));
		}
		Form<User> editForm = new Form<User>(User.class).fill(currentUser);
		editForm.get().password = "";
		return ok(edit.render(editForm, currentUser));
	}

	public static Result update(String username) {
		Form<User> submit = Form.form(User.class).bindFromRequest();
		User currentUser = SessionHelper.currentUser(ctx());
		if (submit.hasErrors() || submit.hasGlobalErrors()) {
			return ok(edit.render(submit, currentUser));
		}
		User u = submit.get();
		u.id = currentUser.id;
		if (!User.update(u)) {
			return ok(edit.render(submit, currentUser));
		}
		SessionController.loginUser(u.username);
		return redirect("/@" + u.username);
	}
	
	/**
	 * HAndles the follower relation
	 * @param id the user the current user is following
	 * @return redirect to to the followed users profile page
	 */
	public static Result follow(long id){
		User.addFollower(id, SessionHelper.currentUser(ctx()));
		return redirect(routes.UserController.show(User.find(id).username));
	}

	@Security.Authenticated(CurrentUserFilter.class)
	public static Result delete(String username) {
		User currentUser = SessionHelper.currentUser(ctx());
		if (currentUser.username.equals(username)
				|| SessionHelper.isAdmin(ctx()))
			User.deleteUser(username);
		return redirect("/users");
	}
	
	

}
