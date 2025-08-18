// ========== TAB SWITCHING ==========
document.querySelectorAll('.tabs button').forEach(btn => {
  btn.addEventListener('click', () => {
    document.querySelectorAll('.tabs button').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    btn.classList.add('active');
    document.getElementById(btn.dataset.tab).classList.add('active');
  });
});

// ========== FETCH ANCESTRIES ==========
fetch("get_ancestries.php")
  .then(res => res.json())
  .then(data => {
    const select = document.getElementById("ancestry");
    data.forEach(item => {
      const opt = document.createElement("option");
      opt.value = item.ancestry_name;
      opt.textContent = item.ancestry_name;
      select.appendChild(opt);
    });
  });

// ========== COMBINED ANCESTRY HANDLER (HERITAGE + FEATS) ==========
document.getElementById("ancestry").addEventListener("change", async function () {
  const ancestry = this.value;
  const heritageSelect = document.getElementById("heritage");
  const featSelect = document.getElementById("ancestry-feat");

  // Reset both
  heritageSelect.innerHTML = '<option value="">-- Select Heritage --</option>';
  featSelect.innerHTML = '<option value="">-- Select Ancestry Feat --</option>';

  if (!ancestry) return;

  try {
    const heritageRes = await fetch("get_heritages.php?ancestry=" + encodeURIComponent(ancestry));
    const heritageData = await heritageRes.json();
    heritageData.forEach(row => {
      const opt = document.createElement("option");
      opt.value = row.heritage_name;
      opt.textContent = row.heritage_name;
      heritageSelect.appendChild(opt);
    });
  } catch (err) {
    console.error("Heritage error:", err);
  }

  try {
    const featRes = await fetch("get_ancestry_feats.php?ancestry=" + encodeURIComponent(ancestry));
    const featData = await featRes.json();
    featData.forEach(row => {
      const opt = document.createElement("option");
      opt.value = row.feat_name;
      opt.textContent = `${row.feat_name} (Level ${row.feat_level})`;
      featSelect.appendChild(opt);
    });
  } catch (err) {
    console.error("Ancestry feat error:", err);
  }
});

// ========== BACKGROUNDS ==========
fetch("get_background.php")
  .then(res => res.json())
  .then(data => {
    const select = document.getElementById("background");
    data.forEach(item => {
      const opt = document.createElement("option");
      opt.value = item.background_name;
      opt.textContent = item.background_name;
      select.appendChild(opt);
    });
  });

// ========== CLASSES ==========
fetch("get_class.php")
  .then(res => res.json())
  .then(data => {
    const select = document.getElementById("class");
    data.forEach(item => {
      const opt = document.createElement("option");
      opt.value = item.class_name;
      opt.textContent = item.class_name;
      select.appendChild(opt);
    });
  });

// ========== CLASS FEATS ==========
async function loadClassFeats() {
  const classSelect = document.getElementById("class");
  const classFeatSelect = document.getElementById("class-feat");

  try {
    const res = await fetch("get_class_feats.php");
    const feats = await res.json();

    classSelect.addEventListener("change", () => {
      const selected = classSelect.value;
      classFeatSelect.innerHTML = '<option value="">-- Select Class Feat --</option>';

      if (feats[selected]) {
        feats[selected].forEach(feat => {
          const opt = document.createElement("option");
          opt.value = feat;
          opt.textContent = feat;
          classFeatSelect.appendChild(opt);
        });
      }
    });
  } catch (err) {
    console.error("Class feats error:", err);
  }
}
loadClassFeats();

// ========== ABILITIES ==========
fetch("get_abilities.php")
  .then(res => res.json())
  .then(data => {
    const table = document.getElementById("ability-table");
    data.forEach(ability => {
      const row = document.createElement("tr");
      row.innerHTML = `
        <td>${ability.ability_name}</td>
        <td><input type="number" value="10" class="base-score"></td>
        <td><input type="number" value="0" class="boost-score"></td>
        <td class="total-score">10</td>
      `;
      table.appendChild(row);
    });

    table.addEventListener("input", e => {
      if (e.target.classList.contains("base-score") || e.target.classList.contains("boost-score")) {
        const row = e.target.closest("tr");
        const base = parseInt(row.querySelector(".base-score").value) || 0;
        const boost = parseInt(row.querySelector(".boost-score").value) || 0;
        row.querySelector(".total-score").textContent = base + boost;
      }
    });
  });

// ========== SKILLS ==========
const rankBonuses = { "Untrained": 0, "Trained": 2, "Expert": 4, "Master": 6, "Legendary": 8 };

fetch('get_skills.php')
  .then(res => res.json())
  .then(data => {
    const list = document.getElementById('skills-list');
    data.forEach(skill => {
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>${skill.skill_name}</td>
        <td><select class="skill-rank">
          ${Object.keys(rankBonuses).map(rank => `<option value="${rank}">${rank}</option>`).join('')}
        </select></td>
        <td class="skill-total">0</td>
      `;
      list.appendChild(row);
    });

    list.addEventListener('change', e => {
      if (e.target.classList.contains('skill-rank')) {
        const row = e.target.closest('tr');
        const rank = e.target.value;
        row.querySelector('.skill-total').textContent = rankBonuses[rank];
      }
    });
  });

// ========== SAVE & LOAD ==========
document.getElementById("save-btn").addEventListener("click", () => {
  const data = {
    name: document.getElementById("char-name").value,
    ancestry: document.getElementById("ancestry").value,
    heritage: document.getElementById("heritage").value,
    ancestryFeat: document.getElementById("ancestry-feat").value,
    background: document.getElementById("background").value,
    class: document.getElementById("class").value,
    classFeat: document.getElementById("class-feat").value,
    abilities: [],
    skills: []
  };

  document.querySelectorAll("#ability-table tr").forEach(row => {
    const ability = row.cells[0]?.textContent;
    const base = row.querySelector(".base-score")?.value;
    const boost = row.querySelector(".boost-score")?.value;
    if (ability) {
      data.abilities.push({ ability, base, boost });
    }
  });

  document.querySelectorAll("#skills-list tr").forEach(row => {
    const skill = row.cells[0]?.textContent;
    const rank = row.querySelector(".skill-rank")?.value;
    if (skill) {
      data.skills.push({ skill, rank });
    }
  });

  localStorage.setItem("characterData", JSON.stringify(data));
  alert("Character saved!");
});

document.getElementById("load-btn").addEventListener("click", () => {
  const saved = localStorage.getItem("characterData");
  if (!saved) return alert("No saved character.");

  const data = JSON.parse(saved);

  document.getElementById("char-name").value = data.name || "";
  document.getElementById("ancestry").value = data.ancestry || "";
  document.getElementById("background").value = data.background || "";
  document.getElementById("class").value = data.class || "";

  document.getElementById("ancestry").dispatchEvent(new Event("change"));
  document.getElementById("class").dispatchEvent(new Event("change"));

  setTimeout(() => {
    document.getElementById("heritage").value = data.heritage || "";
    document.getElementById("ancestry-feat").value = data.ancestryFeat || "";
    document.getElementById("class-feat").value = data.classFeat || "";

    document.querySelectorAll("#ability-table tr").forEach(row => {
      const ability = row.cells[0]?.textContent;
      const abilityData = data.abilities.find(a => a.ability === ability);
      if (abilityData) {
        row.querySelector(".base-score").value = abilityData.base;
        row.querySelector(".boost-score").value = abilityData.boost;
        row.querySelector(".total-score").textContent = parseInt(abilityData.base) + parseInt(abilityData.boost);
      }
    });

    document.querySelectorAll("#skills-list tr").forEach(row => {
      const skill = row.cells[0]?.textContent;
      const skillData = data.skills.find(s => s.skill === skill);
      if (skillData) {
        row.querySelector(".skill-rank").value = skillData.rank;
        row.querySelector(".skill-total").textContent = rankBonuses[skillData.rank];
      }
    });
  }, 200); // delay for drop down
});
