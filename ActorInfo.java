Copyright 2014  M3Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
package com.t3.metamediamanager;

/*
 * Class used to store and share information about an actor in a given media
 */
public class ActorInfo {
	private String _name="";
	private String _role="";
	private String _imgUrl="";
	
	public ActorInfo()
	{
		
	}
	
	public ActorInfo(String name)
	{
		this.setName(name);
	}
	
	public ActorInfo(String name, String role, String imgUrl)
	{
		this.setName(name);
		this.setRole(role);
		this.setImgUrl(imgUrl);
	}
	
	public String getName() {
		return _name;
	}

	public void setName(String _name) {
		this._name = _name;
	}

	public String getRole() {
		return _role;
	}

	public void setRole(String _role) {
		this._role = _role;
	}

	public String getImgUrl() {
		return _imgUrl;
	}

	public void setImgUrl(String _imgUrl) {
		this._imgUrl = _imgUrl;
	}
}
