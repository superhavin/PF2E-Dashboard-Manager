# A Dashboard Manager for PF2e

⚠ This software is not supported, endorsed, or approved by Paizo Inc, or any associated groups.

Pathboard allows you to create and share rules and sheets for PF2e tabletop system. This allow GMs to better present the rulebook to Players; or even allow Players to share their character sheets with each other. Additionally, this application will support custom content, so that Homebrewers can share their content easily.


THIS NEEDS XAMAPP AND PHPmyAdmin to work 

1.Install XAMAPP and when it presents all the things you watn to install with it make sure you choose APACHE and MYSQL. (PHPMyadmin with this dowload)
XAMAPP has its own MYSQL so you might have to close other MYSQL that are using the port

2. Once XAMAPP is installed boot it up and start apache and MYSQL from there you can go to PHPAdmin

3. To check if PHPMyadmin works you can put http://localhost/phpmyadmin/ into the browser which should open up Databases Server Page 

4. On the left sidebar you can press new to make a new database and name it(I named it pathfinder_dashboard)

5. to insert actual content in PHPMYadmin press the SQL button which will open up a window and run Main from inteliji  to generate the SQL code to copy into PHPmyAdmin SQL and run 

6. once the databases is populated, take the Index.html, style.css, and all the PHP files and put them into htdocs which can be found by going to XAMAPP and pressing explorer which will open all files and just find htdocs and put them there.

7. then once thats set up you can go back to your browser and put this in to see the result http://localhost/index.html


