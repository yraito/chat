#Since we do not store the plaintext passwords anywhere, we make the passwords easy
#to remember: Password = Username for all these records
 
#Create a few user accounts
INSERT INTO Users 
	(Username, Passhash, Email, Created) VALUES
    ('YoYoMa', '1000:7e8577e6dd71d97708bb71b474c8a8f670286bd120b2a6cd:41ce5b44cc448b1ddcf10a59b211ee72660504b108eafa78', 'yoyoma@yahoo.com', 1453129550000),
    ('TomBrady', '1000:7609723ed5a44c069013f3c9c6309b3c5a16c5ba28fe8895:a902564eeb6d4721bf751cd7332470b92e7edae765ab7c41', 'tombrady@tombrady.tombrady', 1453129560000),
    ('AngelinaJolie', '1000:e0a0eedc37abb7e2a526d6c04d2625fec76ace06458859dc:547462644f85f989d41c42d2c3abbec0aa154a7c7f74ebd4', 'angie@gmail.com', 1453129570000),
    ('IsaacNewton', '1000:6842e454942b4ca2118ec2c22251825d4c2217b0ea4b372c:80f3b90a53e5583eaf0239188263cef43a8c791707ff24a1', 'newton@fig.com', 1453129580000),
    ('BillGates', '1000:288ce1aeffdebe165ab551a49f483758330f7cab9bdf8959:e15ec3842c458d1bb906a19f069f176b80aca8f354a210b3', 'bgates@apple.com', 1453129590000);
	
#A couple software agent accounts
INSERT INTO Users
	(OwnerId, Username, Passhash, Uuid, Created) VALUES
    (2, 'bradybot0', '1000:2b8d89ccc3b301ddb0b738be24141ad4847d773ac0c7b51e:b12c09acbaf2e87d3eb0dcd037b250ad53fe9c0791201a04', 'abcdefghijklmnop', 1453129560001),
    (2, 'bradybot1', '1000:3426ef56a4c9f334cb99d6dcba925e13bade3191b313dd2a:a6845e2362b743d8bbbd73d6658289851b2e0d5973fd24c3', 'qrstuvwxyz012345', 1453129560002);
 
#And a couple admin accounts 
INSERT INTO Admins
	(Username, Passhash, Email, Created) VALUES
    ('admin1', '1000:8331021e541ed52de98f604f64b5492f5bd98d58506b6339:dd66574e7d12bf213623814c2aaf2cda4398e9f9f02cc5e9', 'blah@blah.blah', 1453129000000),
    ('admin2', '1000:5e664a4475c46c7a52df3c07936171c07fc9f52438d2ac35:7a976b23d5f6195ae08134efe89b7d6160651e993df6fef2', 'blah2@blah.blah', 1453129100000);