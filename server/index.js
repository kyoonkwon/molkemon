require('dotenv').config();

var express = require('express');
var app = express();
var server = require('http').createServer(app);
var io = require('socket.io')(server);

const mysql   = require('mysql');
const connection = mysql.createConnection({
  host     : 'localhost',
  user     : process.env.DBUSER,
  password : process.env.DBPASS,
  database : process.env.DBNAME
});
const port = 80;

io.on('connection', (socket) => {
	console.log("user connect");
	
	socket.on("userInfo", obj => {
		console.log(obj.user_id + " login");
		login(obj.user_id, socket);
	})
	
	socket.on("change", obj => {
		console.log(obj.user_id + " changed");
		update(obj)
	})
	
	socket.on("register", obj => {
		console.log(obj.user_id + " register");
		register(obj, socket);
		
	})
	
	socket.on('disconnect', () => {
		console.log('user disconnect')
	});
})

server.listen(port, function(){
	console.log(`server open at port ${port}`);
})


app.get("/", (req, res) => {
  res.send("OK");

})

function register(obj, socket){
	
	
	var userInfo = {};
  	var pokemonInfo = {};
	
			
	connection.query('SELECT MAX(id) FROM users', (_, row, __) => {
		var user_id = row[0]['MAX(id)']+1

		connection.query('SELECT MAX(id) FROM pokemon', (_, prow, __) => {
			var poke_id = prow[0]['MAX(id)']+1
			var skills = [{"id": 1, "level":1}];
			connection.query(`INSERT INTO users (id, kakao_id, name, pokemon_id, coin, class) 
			VALUES (${user_id}, '${obj.user_id}', '${obj.name}', ${poke_id}, 0, ${obj.classValue})`,
							(_, __, ___) => {
				connection.query(`INSERT INTO pokemon (id, level, skills, exp, number)
				VALUES (${poke_id}, 1, JSON_MERGE_PATCH(skills, '${JSON.stringify(skills)}'), 0, ${obj.pokeNum})`, 
								(_, __, ___) => {
					
					userInfo["coin"] = 0;
					userInfo["name"] = obj.name;
					pokemonInfo["id"] = poke_id;
					pokemonInfo["level"] = 1;
					pokemonInfo["number"] = obj.pokeNum;
					pokemonInfo["exp"] = 0;
					skills[0]["name"] = "몸통박치기"
					skills[0]["cool"] = 5
					skills[0]["power"] = 10
					pokemonInfo["skills"] = skills;
					userInfo["pokemon"] = pokemonInfo;
					userInfo["guild"] = obj.classValue;
					
					//console.log(JSON.stringify(userInfo));
					socket.emit("registerDone", userInfo);
				});

			});
		})
	});
	

}

function update(obj){
	
	var userQuery = `UPDATE users SET coin = ${obj.coin} WHERE kakao_id='${obj.user_id}'`;
	var skills = obj.poke.skills;
	var pokemonQuery = `UPDATE pokemon SET level = ${obj.poke.level}, exp = ${obj.poke.exp}, skills = JSON_MERGE_PATCH(skills, '${JSON.stringify(skills)}'), number = ${obj.poke.number} WHERE id='${obj.poke.id}'`;

	connection.query(userQuery);
	connection.query(pokemonQuery);
}


// 유저가 kakao id를 보낼 때
function login(user_id, socket){
	
  	var loginResult = {};
  	var userInfo = {};
  	var pokemonInfo = {};
  	var pokemon_id;
	  
     
     connection.query(`SELECT * from users WHERE kakao_id='${user_id}'`,
     (_, userRow, __) => {
       
        if(userRow.length > 0){
          userInfo["coin"] = userRow[0].coin;
		  userInfo["name"] = userRow[0].name;
          pokemon_id = userRow[0].pokemon_id;
        
          connection.query(`SELECT * from pokemon WHERE id='${pokemon_id}'`, 
          (_, pkmRow, __) => {

			pokemonInfo["id"] = pkmRow[0].id;
            pokemonInfo["level"] = pkmRow[0].level;
            pokemonInfo["number"] = pkmRow[0].number;
            pokemonInfo["exp"] = pkmRow[0].exp;
			pokemonInfo["guild"] = pkmRow[0].class;

            var skills = JSON.parse(pkmRow[0].skills)
            var skillInfoes = []

            connection.query(`SELECT * from skills WHERE id IN(${skills.map(x => `'${x.id}'`).join(', ')})`,
            (_, skillRow, __) => {

              skillRow.forEach((elem, idx) => {
                skillInfo = {}
				skillInfo["id"] = elem.id;
                skillInfo["name"] = elem.name;
                skillInfo["cool"] = elem.cool;
                skillInfo["power"] = elem.power;
                skillInfo["level"] = skills[idx].level;
                skillInfoes.push(skillInfo);
              });

              pokemonInfo["skills"] = skillInfoes;
              userInfo["pokemon"] = pokemonInfo;
				
			  socket.emit("userInfo", userInfo);
            });
          });
        } else{
			socket.emit("register", {"user_id": user_id});
		}

	});
}