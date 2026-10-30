# Background information

This project is based on a homework I received for a job interview I had in August 2026.
I continued to develop it in order to learn new skills and experiment.

## Backend
Springboot application. The basic skeleton was built on the following requirements:

### Digital Cliche Piggy Bank

#### Objective:
* Develop a digital “Cliche piggy bank” as an MVP

#### Background:
* A “cliché piggy bank” is, in its standard physical form, a hollow container —often shaped like a pig— into which anyone caught using a cliché must drop a fine.
* To encourage greater self-discipline among colleagues regarding the use of “empty” phrases —and simply as a bit of shared fun— such a cliché piggy bank was recently introduced.
* However, it is located in the office, making its use impractical during periods of working from home.
* To continue penalizing the use of clichés even under these circumstances, a digital solution is therefore needed.

#### Requirement Definition Method
* The requirements are formulated as so-called user stories.
* They therefore follow this structure: First, the role or perspective of the person defining the requirement is specified, followed by a description of the actual requirement, and finally the objective behind the requirement. This is supplemented by acceptance criteria, which are to be understood as acceptance criteria and are therefore either presented during handover or tested by the client to verify that the respective requirement has been met.
* The requirements are prioritized and thus also represent the possible stages of an MVP:
    * Stage 1: Basic Functions
    * Stage 2: Role and Permission Management, Part 1
    * Stage 3: Role and Permission Management, Part 2 (You will need to decide for yourself whether Part 2 truly increases complexity and should therefore be developed in a subsequent phase)

After each level, a handover takes place, including a presentation and/or testing of the acceptance criteria.
At least Level 1 must be completed. Whether Levels 2 and 3 will also be implemented will be decided based on the remaining time available.

| Priority  	| As a...  	| I want to...  	| in order to...  	| Acceptance criterion  	|
|---	|---	|---	|---	|---	|
| 1  	| As a piggibank user  	| penalize other users  	| condone the use of a hollow phrase  	| * Sebastian gives Alex a standard penalty (1€), and the corresponding amount is then credited to Alex's account.  	|
| 1  	| As a piggibank user  	| have an account into which penalty payments can be deposited  	| be able to accumulate multiple sanctions  	| * Alex gives me a standard penalty, which then appears in my account<br/> * Alex gives me another penalty, and this is added to the first amount and appears in my account  	|
| 1  	| As a piggibank user  	| reset another user's balance to 0,-  	| document the physical payment of outstanding balances  	| * Alex has a balance in his account, and I'm resetting it to 0,-  	|
| 1  	| As a piggibank user  	| enter the reason or the penalized phrase into a free-text field  	| record the reason so that I'll have an overview later.  	| * Sebastian gives Alex a standard penalty and enters “lorem ipsum,” which is then displayed on the home page of the digital suggestion box in a list of the last 10 penalties, along with the person in question  	|
| 1  	| As a piggibank user  	| have an overview of all free-text reasons  	| have a reminder of the funniest moments  	| * I click the “Show All” button next to the list of the last 10 sanctions and see a list of all entries  	|
| 1  	| As a piggibank user  	| that there is an administrator role held by an external person  	| have a neutral authority  	| * Explanation: Ideally, a fourth person—who is not a regular user—should be assigned an admin role with different permissions. If it’s easier to implement this as a dual role for one of the existing users, that would also be acceptable, but in the interest of neutrality, Solution 1 is the preferred approach.<br/> * Please define your own acceptance criteria as appropriate.  	|
| 2  	| As administrator of the piggibank  	| be able to add people  	| be able to expand the user base as needed  	| * In addition to the original participants—Alex, Heidrun, and Sebastian—Thomas should be added  	|
| 2  	| As administrator of the piggibank  	| be able to remove or deactivate people  	| be able to reduce the user base if necessary  	| * The administrator first adds Colin as an additional user (see the user story about this) and then removes him again  	|
| 2  	| As administrator of the piggibank  	| be able to remove a person only once the outstanding balance is 0,-  	| ensure that the debts have been paid off first  	| * The administrator adds Colin as an additional user, assigns him an amount, and tries to remove him → this should not be possible. Instead, a warning message should appear stating that there is still an outstanding amount.<br/> * The administrator clears the outstanding amount, attempts to remove or deactivate the user, and the operation is successful.  	|
| 3  	| As administrator of the piggibank  	| be able to set the balances in a user account to 0.00  	| to increase security compared to the simple solution described above  	| * As a logged-in administrator, I reset Alex's account (which has a balance)<br/> * As a logged-in user, this option is not available to me / it does not work  	|
| 3  	| As a piggibank user  	| impose penalties of varying amounts on other users  	| have more flexibility in determining the severity of penalties  	| * Sebastian gives Alex all three of the three possible amounts (0.50, 1, 2 €) one after another, which are then added up and credited to his account.  	|
| 3  	| As a piggibank user  	| vote on the list of reasons  	| be able to choose a “Best Of”  	| * On the list of all entries, I can click a “Like” button for each entry (only one ‘Like’ per user per entry). An additional column in the list displays the total number of “Likes,” and the list is sorted according to that total.  	|

## Frontend
Created with React. A landing page is provided, that guides to three dedicated pages for:
* Users
* Accounts
* Sanctions

The featuers are added progressively. Whenever the frontend suggests a modification of the frontend, this is also adapted.