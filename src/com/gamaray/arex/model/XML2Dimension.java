/*
    This file is part of betelgeuse.

    betelgeuse is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    betelgeuse is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with betelgeuse.  If not, see <http://www.gnu.org/licenses/>.

 */
package com.gamaray.arex.model;

import com.gamaray.arex.xml.Element;

public class XML2Dimension {

	public Dimension buildDimension(Element elem) {
		
		CreatedDimension dim = new CreatedDimension();
		
		String name = elem.getChildElementValue("name", "NO_NAME");
		dim.setName(name);
		
		return dim;
	}
	
	
	private static class CreatedDimension implements Dimension {
		
		private String name;
		
		public String getName() {
			return this.name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
	}
}
