/*
Copyright (C) 2003 WTWG, Uni Bern
 
This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.
 
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
 
You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA
 
Author: Christian Luginbuehl, 01.05.2003 , Version Alcatraz 0.5
*/

function set_project(search,result){
  var navigio_frame=document.getElementById('navigio_frame');
  var navigio_result_frame=document.getElementById('resultframe');
  var navigio_splitter=document.getElementById('navigio_splitter');
  navigio_frame.setAttribute('src',search);
  navigio_frame.setAttribute('flex','1');
  navigio_result_frame.setAttribute('src',result);
  navigio_result_frame.setAttribute('flex','1');
  navigio_splitter.setAttribute('state','open');
  navigio_splitter.setAttribute('collapse','before');
   
}
